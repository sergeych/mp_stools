package net.sergeych.mp_logger

import kotlinx.coroutines.runBlocking

internal actual fun ConsoleLoggerSetup() {
    // we do not want our log to loose last mesages on shutdown:
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking { Log.disconnectConsole() }
    })
}