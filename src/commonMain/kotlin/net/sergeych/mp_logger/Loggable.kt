package net.sergeych.mp_logger

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

interface Loggable {
    var logTag: String
    var logLevel: Log.Level?
}

fun Loggable.addLog(level: Log.Level,reporter: ()->LogData) {
    if( logLevel ?: Log.defaultLevel <= level )
        Log.add(level, reporter)
    // TODO
}

fun Loggable.info(reporter: ()->String) {
    addLog(Log.Level.INFO) { LogData.Message(logTag, reporter()) }
}

fun Loggable.debug(reporter: ()->String) {
    addLog(Log.Level.DEBUG) { LogData.Message(logTag, reporter()) }
}

fun Loggable.warning(reporter: ()->String) {
    addLog(Log.Level.WARNING) { LogData.Message(logTag, reporter()) }
}

fun Loggable.error(reporter: ()->String) {
    addLog(Log.Level.ERROR) { LogData.Error(logTag, reporter()) }
}

fun Loggable.exception(reporter: ()-> Pair<String,Throwable>) {
    addLog(Log.Level.WARNING) {
        val (message, exception) = reporter()
        LogData.Error(logTag, "$message:${exception.message}", exception.stackTraceToString() )
    }
}

@Serializable
sealed class LogData {
    abstract val tag: String
    abstract val message: String

    class Message(override val tag: String, override val message: String) : LogData()

    class Error(
        override val tag: String,
        override val message: String,
        val stack: String? = null
    ) : LogData()
}

@Serializable
data class LogEntry(val level: Log.Level, val data: LogData,val timestamp: Instant) {
    override fun toString(): String {
        return super.toString()
    }
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

    fun add(level: Level,reporter: suspend ()->LogData) {
        // We want to stamp time close to report time, launch could be considerably post current time
        // and collecting log data could add even more:
        val instant = Clock.System.now()

        GlobalScope.launch {
            log.emit(LogEntry(level, reporter(), instant))
        }
    }
}

//expect fun LogToConsole()

