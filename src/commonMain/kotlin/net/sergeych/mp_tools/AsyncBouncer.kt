package net.sergeych.mp_tools

import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.time.Instant
import net.sergeych.mptools.Now
import net.sergeych.mptools.withReentrantLock
import kotlin.time.Duration

/**
 * Expermiental multiplatform coroutine-based bouncer: safe way to call some suspend code
 * after a timeout. It is thread-safe (where multithreaded) and coroutine-safe.
 *
 * Note that creating a bouncer will not invoke its callback until the corresponding pulse call.
 *
 * @param initialTimeout default timeout for [pulse], could be changed at runtime assigning to [timeout].
 * @param initialMaxTimeout maximum timeout between calls, systemm will invoke callback when it expires even if there
 *              will be [pulse] calls in between. Could be changed with [maxTimeout]
 * @param callback what to invoke.
 */
class AsyncBouncer(
    initialTimeout: Duration,
    initialMaxTimeout: Duration = initialTimeout,
    callback: suspend () -> Unit,
) {

    private var lastCallAt: Instant? = null
    private var callAt: Instant? = null
    private val access = Mutex()
    private val pulseChannel = Channel<Int>(0, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * Default time between call to [pulse] and invocation. Assigning calls [pulse].
     */
    var timeout: Duration = initialTimeout
        set(value) {
            field = value
            pulse()
        }

    /**
     * Maximim time between invocations: even if [pulse] is being called more often, invocations will happen at this
     * rate. Assigning it calls [pulse]
     */
    var maxTimeout: Duration = initialMaxTimeout
        set(value) {
            field = value
            pulse()
        }

    /**
     * Cause a block to be scheduled after [timeout] or right now from now even it is already being executing.
     * To change effective [timeout], assign a value to it _prior to call pulse_.
     */
    fun pulse(now: Boolean = false) {
        checkNotClosed()
        globalLaunch {
            access.withReentrantLock {
                callAt = if (now) Now() else Now() + timeout
                lastCallAt?.let {
                    val limitTime = it + maxTimeout
                    if (callAt!! > limitTime) callAt = limitTime
                }
                pulseChannel.send(1)
            }
        }
    }

    /**
     * Perform a callback exclusively, e.g. nouncer callback is guatemteed not to be active while
     * callback is performed, then pulse bouncer()
     */
    suspend fun performAndPulse(now: Boolean = false, block: suspend () -> Unit) {
        access.withReentrantLock {
            checkNotClosed()
            block()
            pulse(now)
        }
    }

    private fun checkNotClosed() {
        if (isClosed) throw IllegalStateException("Bounced is closed")
    }

    private var job: Job? = null
    private var stop = false

    val isClosed: Boolean get() = job == null

    /**
     * Safely close the bouncer freeing its performer coroutine: if it is being executed or scheduled to execute
     * (pulsed) it will perform the block and wait for it to finish. Calling it on closed bounced has
     * no effect.
     */
    suspend fun close() {
        access.withReentrantLock {
            if (job != null) {
                stop = true
                pulse(true)
            }
        }
        job?.join()
        job = null
    }


    init {
        job = globalLaunch {
            do {
                val duration = callAt?.let { it - Now() } ?: Duration.INFINITE
                try {
                    if (duration > Duration.ZERO) {
                        withTimeout(duration) {
                            pulseChannel.receive()
                        }
                    }
                } catch (_: TimeoutCancellationException) {
                    // expected
                }
                access.withReentrantLock {
                    if (callAt?.let { it <= Now() } == true) {
                        callAt = null
                        try {
                            callback()
                            callAt = null
                            lastCallAt = Now()
                        } catch (t: Throwable) {
                            // we can't use logging here as logger uses us ;)
                            println("unexpected error in AsyncBouncer: $t")
                            t.printStackTrace()
                        }
                    }
                }
            } while (!stop)
        }
    }
}
