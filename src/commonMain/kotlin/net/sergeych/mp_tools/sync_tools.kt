package net.sergeych.mp_tools

interface MPSyncObject {
    fun <T>withLock(block: ()->T): T
}

/**
 * The platform-dependent micro mutually-excelusive exection. WARNING! It does not (yet) works on native where
 * multithreading is actually not supported whatever jetbrains thinks (as for march 2022). So on native platform
 * it hust calls the block...
 */
expect fun MSyncObject(): MPSyncObject