package net.sergeych.mp_tools

import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Instant
import net.sergeych.mptools.Now
import net.sergeych.mptools.withReentrantLock
import kotlin.time.Duration

/**
 * Expermiental multiplatform coroutine-based bouncer: safe way to call some suspend code
 * after a timeout. It is thread-safe (where multithreaded) and coroutine-safe.
 */
class AsyncBouncer(var timeout: Duration, callback: suspend () -> Unit) {

    private var callAt: Instant? = null
    private val access = Mutex()
    private val pulseChannel = Channel<Int>(0, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * Cause a block to be scheduled after [timeout] or right now from now even it is already being executing.
     */
    suspend fun pulse(now: Boolean = false) {
        access.withReentrantLock {
            checkNotClosed()
            callAt = if (now) Now() else Now() + timeout
            pulseChannel.send(1)
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
                        }
                        catch(t: Throwable) {
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
