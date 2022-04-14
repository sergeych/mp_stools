@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
@file:Suppress("VARIABLE_IN_SINGLETON_WITHOUT_THREAD_LOCAL")

package net.sergeych.mp_logger

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.sergeych.mp_logger.Log.connectConsole
import net.sergeych.mp_logger.Log.logFlow
import net.sergeych.sprintf.sprintf

/**
 * Class/object instance that could be used to emit logging from. See [info], [debug], [warning], [error] and
 * [exception] Loggable excension functions. The interface is used as a workaround of missing multimple inheritance
 * in kolin, in most cases you just inherit from [LogTag]:
 *
 * ~~~kotlin
 * // simple case
 * class SimpleClass(): LogTag("SCLAS") {
 *   fun test() {
 *      debug { "test1" }
 * }
 *
 * // but if we need to inherit some other class, we need an interface:
 *
 * class StrangeException(text: String): Exception(text), Loggable by TagLog("SEXC") {
 *      init {
 *          debug { "StrangeException has been instantiated" }
 *      }
 * }
 * ~~~
 */
interface Loggable {
    /**
     * Tag attached to each emitted log entry. Better be 3-5 character long, though when really need it could
     * be any string.
     */
    var logTag: String

    /**
     * Filtering level _for this instance only__. It takes precedence over [Log.defaultLevel] if not null, e.g
     * if the overall logging level is set ot [Log.Level.INFO] but this instance is set to [Log.Level.DEBUG],
     * the debug log records _from this instance_ will be accepted.
     */
    var logLevel: Log.Level?
}

/**
 * A tag that allow to log with using one of [info], [debug], [warning], [error] and [exception].
 *
 * It is open class, so you can just inherit from it. If you can't, for example, because your class has already
 * inherits from one, use it as delegate with [Loggable] interface:
 * ~~~kotlin
 * class StrangeException(text: String): Exception(text), Loggable by TagLog("SEXC") {
 *      init {
 *          debug { "StrangeException has been instantiated" }
 *      }
 * }
 * ~~~
 */
open class LogTag(override var logTag: String, override var logLevel: Log.Level? = null) : Loggable

/**
 * Add log enctry of the specified level. If the log level does not permit message to be logged, its [reporter]
 * will not be called at all, giving minimum impact on the production system or like.
 *
 * Usually, this method is not directly called, instead, use one of [info], [debug], [warning], [error] and
 * [exception].
 *
 * __Important note__ the reporter could be called in a separated thread, the logger preserves creation time as
 * close as it can but perform formatting/collection or wharever else `reporter()` does in a separate coroutine. So
 * if you really need some varying value or function result to be captured when reporting, save it in a local variable
 * before calling `addLog` and use its captured value inside.
 *
 * @param level level of this message
 * @param reporter function that creates actual log record when needed.
 */
fun Loggable.addLog(level: Log.Level, reporter: () -> LogData) {
    if ((logLevel ?: Log.defaultLevel).priority <= level.priority)
        Log.add(level, reporter)
}

/**
 * Conditionally emits a [Log.Level.INFO] - level log message. See [addLog] for details.
 */
fun Loggable.info(reporter: () -> String) {
    addLog(Log.Level.INFO) { LogData.Message(logTag, reporter()) }
}

/**
 * Conditionally emits a [Log.Level.DEBUG] - level log message. See [addLog] for details.
 */
fun Loggable.debug(reporter: () -> String) {
    addLog(Log.Level.DEBUG) { LogData.Message(logTag, reporter()) }
}

/**
 * Conditionally emits a [Log.Level.WARNING] - level log message. See [addLog] for details.
 */
fun Loggable.warning(reporter: () -> String) {
    addLog(Log.Level.WARNING) { LogData.Message(logTag, reporter()) }
}

/**
 * Conditionally emits a [Log.Level.ERROR] - level log message. See [addLog] for details.
 */
fun Loggable.error(reporter: () -> String) {
    addLog(Log.Level.ERROR) { LogData.Error(logTag, reporter()) }
}

/**
 * Conditionally emits a [Log.Level.ERROR] - level log message adn attatch a throwable excetion object to it,
 * with its stack trace in particular. Reported function must return a [Pair] where first value is a log message
 * and the second is an exception instance. If tiy don't need one, use [error] instead. See [addLog] for details.
 */
fun Loggable.exception(reporter: () -> Pair<String, Throwable>) {
    addLog(Log.Level.ERROR) {
        val (message, exception) = reporter()
        LogData.Error(logTag, "$message:${exception.message}", exception.stackTraceToString())
    }
}

/**
 * The logged data serializable container.
 */
@Serializable
sealed class LogData {
    /**
     * [Loggable#logTag] of the message
     */
    abstract val tag: String

    /**
     * The log message itself
     */
    abstract val message: String

    /**
     * The regular log message: no additional data
     */
    class Message(override val tag: String, override val message: String) : LogData() {
        override fun toString(): String = message
    }

    /**
     * Error log message: also an optional stack.
     */
    class Error(
        override val tag: String,
        override val message: String,
        val stack: String? = null
    ) : LogData() {
        override fun toString(): String = "$message${stack?.let { "\n$it" } ?: ""}"
    }
}

/**
 * Log entry savesa log level and creation timestamp, as its actual consumption by subscribers (see [Log.logFlow])
 * happens later, sometimes considerably.
 */
@Serializable
data class LogEntry(val level: Log.Level, val data: LogData, val timestamp: Instant) {
    override fun toString(): String = "%tT %c %-5s %s".sprintf(timestamp, level.name[0], data.tag, data)
}

/**
 * Shared log state and tools. Use [Loggable] to emit log entries, [logFlow] to collect them (there is also replay
 * buffer) and service functions such as [connectConsole] to simplify logging.
 */
object Log {

    /**
     * The defaul log level. Attempts to emit messages with a level less than this one will be ignoring without
     * evaluating message preparing code. This behavior could be overridden with [Loggable.logLevel] variable.
     */
    var defaultLevel: Level = Level.INFO

    enum class Level(val priority: Int) {
        HIDDEN(0),
        DEBUG(10),
        INFO(100),
        WARNING(1000),
        ERROR(10000)
    }

    private val log = MutableSharedFlow<LogEntry>(1000, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * The log entries source. It has some replay buffer so new collectors will receive latest log messages. The flow
     * has a limited buffer and drops oldest messages.
     */
    val logFlow = log.asSharedFlow()

    /**
     * Add a log entry Important! this method __does not checks and filters the level__ allowing even low priority
     * messages to be added to the log. This is done intentionally to allow per-instance level filtering. See [Loggable]
     * extension functions to get a proper filtered log emission function.
     */
    fun add(level: Level, reporter: suspend () -> LogData) {
        // We want to stamp time close to report time, launch could be considerably post current time
        // and collecting log data could add even more:
        val instant = Clock.System.now()

        GlobalScope.launch(sequential) {
            log.emit(LogEntry(level, reporter(), instant))
        }
    }

    private val sequential = Dispatchers.Default.limitedParallelism(1)

    /**
     * Launch a block in the non-concurent loggin dispatcher. Use it as an alternative to a muted, but _only when
     * implementing functions closely related to the logging subsystem_. Do not block it!
     */
    fun launchExclusive(block: suspend () -> Unit) {
        GlobalScope.launch(sequential) { block() }
    }

    private var consoleJob: Job? = null
    private var stopConsole: Boolean = false

    /**
     * If console logger is connected via [connectConsole], this will change it filtering level. Default is stored
     * or overriden when console is connected
     */
    var consoleLogLevel = Level.DEBUG

    /**
     * Start (if not already started) emitting log messages to the console (stdout). Due to replay buffer of the log
     * flow, it will immediately emit buffered entries, if any. Repeated calls to it do nothing.
     * @param level if set, overrides current value of [consoleLogLevel] thus filtering only messages with a given
     *              priority and higher.
     */
    fun connectConsole(level: Level? = null) {
        level?.let {
            consoleLogLevel = it
            defaultLevel = it
        }
        launchExclusive {
            if (consoleJob == null) {
                ConsoleLoggerSetup()
                stopConsole = false
                val lf = LogFormatter()
                consoleJob = GlobalScope.launch(Dispatchers.Unconfined) {
                    logFlow.collect { record ->
                        if( stopConsole ) cancel()
                        if( record.level >= consoleLogLevel ) {
                            try {
                                val text = lf.format(record).joinToString("\n")
                                // if we inline it strange bug eats it
                                println(text)
                            } catch (e: Throwable) {
                                println("***** unexpected logger exception: $e")
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Stop emitting log messages to the stdount. First call to it immediately stops console output no matter
     * how many times [connectConsole] was called before. This behavior though might be altered in future.
     */
    suspend fun disconnectConsole() {
        withContext(sequential) {
            consoleJob?.let {
                // Let all emitters a chance to fill the log flows
                yield()
                // poison pill the console logger
                stopConsole = true
                add(Level.HIDDEN, { LogData.Message("CONS", "Shutting down console logger") })
                // wait it to die
                it.join()
                consoleJob = null
            }
        }
    }
}

expect internal fun ConsoleLoggerSetup()

@Suppress("unused")
fun <T>Loggable.ignoreExceptions(from: String?=null, f:()->T): Result<T> {
    return try { Result.success(f()) }
    catch(x: Throwable) {
        exception { "${from ?: this::class.simpleName ?: "?"}: exception thrown: $x" to x }
        Result.failure(x)
    }
}

@Suppress("unused")
suspend fun <T>Loggable.ignoreAsyncExceptions(from: String?=null,f: suspend ()->T): Result<T> {
    return try { Result.success(f()) }
    catch(x: Throwable) {
        exception { "${from ?: this::class.simpleName ?: "?"}: exception thrown: $x" to x }
        Result.failure(x)
    }
}

