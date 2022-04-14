@file:OptIn(DelicateCoroutinesApi::class)

package net.sergeych.mp_tools

import kotlinx.coroutines.*
import net.sergeych.mp_logger.LogTag
import net.sergeych.mp_logger.exception

private val log = LogTag("MPTLS")

fun globalLaunch(block: suspend CoroutineScope.()->Unit): Job =
    GlobalScope.launch {
        try {
            block()
        }
        catch(t: Throwable) {
            log.exception { "unexpected in globalLaunch" to t }
        }
    }