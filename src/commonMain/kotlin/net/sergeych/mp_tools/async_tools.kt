@file:OptIn(DelicateCoroutinesApi::class)

package net.sergeych.mp_tools

import kotlinx.coroutines.*
import net.sergeych.mp_logger.LogTag
import net.sergeych.mp_logger.exception

private val log = LogTag("MPTLS")


/**
 * Launch a standalone cancellable coroutine using GlobalScope. Eats warnings (where possible) and
 * do not report `CancellationException`. Other unhandled exceptions are logged with usual [Log] means.
 */
fun globalLaunch(block: suspend CoroutineScope.()->Unit): Job =
    GlobalScope.launch {
        try {
            block()
        }
        catch(_: CancellationException) {
            // this is OK for global launch
        }
        catch(t: Throwable) {
            log.exception { "unexpected in globalLaunch" to t }
        }
    }


/**
 * Calculate block in standalone coroutine and return its deferred result
 */
@Suppress("unused")
fun <T>globalDefer(block: suspend CoroutineScope.() -> T): Deferred<T> = GlobalScope.async { block() }
