package net.sergeych.mp_logger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.sergeych.sprintf.net.sergeych.mp_logger.sprintf

interface Loggable {
    var logTag: String
    var logLevel: Log.Level?
}

data class LogTag(override var logTag: String, override var logLevel: Log.Level? = null) : Loggable

fun Loggable.addLog(level: Log.Level, reporter: () -> LogData) {
    if ( (logLevel ?: Log.defaultLevel) <= level)
        Log.add(level, reporter)
    // TODO
}

fun Loggable.info(reporter: () -> String) {
    addLog(Log.Level.INFO) { LogData.Message(logTag, reporter()) }
}

fun Loggable.debug(reporter: () -> String) {
    addLog(Log.Level.DEBUG) { LogData.Message(logTag, reporter()) }
}

fun Loggable.warning(reporter: () -> String) {
    addLog(Log.Level.WARNING) { LogData.Message(logTag, reporter()) }
}

fun Loggable.error(reporter: () -> String) {
    addLog(Log.Level.ERROR) { LogData.Error(logTag, reporter()) }
}

fun Loggable.exception(reporter: () -> Pair<String, Throwable>) {
    addLog(Log.Level.WARNING) {
        val (message, exception) = reporter()
        LogData.Error(logTag, "$message:${exception.message}", exception.stackTraceToString())
    }
}

@Serializable
sealed class LogData {
    abstract val tag: String
    abstract val message: String

    class Message(override val tag: String, override val message: String) : LogData() {
        override fun toString(): String = message
    }

    class Error(
        override val tag: String,
        override val message: String,
        val stack: String? = null
    ) : LogData() {
        override fun toString(): String = "$message${stack?.let{"\n$it"} ?: ""}"
    }
}

@Serializable
data class LogEntry(val level: Log.Level, val data: LogData, val timestamp: Instant) {
    override fun toString(): String = "tT%s %c %5s %s".sprintf(timestamp, level.name[0],data.tag, data)
}

object Log {

    val defaultLevel: Log.Level = Level.INFO

    enum class Level(priority: Int) {
        DEBUG(10),
        INFO(100),
        WARNING(1000),
        ERROR(10000)
    }

    private val log = MutableSharedFlow<LogEntry>(1000, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val logFlow = log.asSharedFlow()

    fun add(level: Level, reporter: suspend () -> LogData) {
        // We want to stamp time close to report time, launch could be considerably post current time
        // and collecting log data could add even more:
        val instant = Clock.System.now()

        GlobalScope.launch {
            log.emit(LogEntry(level, reporter(), instant))
        }
    }

    private val sequential = Dispatchers.Default.limitedParallelism(1)

    fun launchExclusive(block: suspend () -> Unit) {
        GlobalScope.launch(sequential) { block() }
    }

    private var consoleConnected = false

    fun connectConsole() {
        launchExclusive {
            if (!consoleConnected) {
                consoleConnected = true
                GlobalScope.launch {
                    logFlow.collect { record ->
                        if( !consoleConnected ) return@collect
                        println(">> $record")
                    }
                }
            }
        }
    }

    fun disconnectConsole() {
        consoleConnected = false
    }
}


