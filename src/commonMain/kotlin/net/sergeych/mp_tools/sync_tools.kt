package net.sergeych.mp_tools

@Deprecated("switch to coroutines")
interface MPSyncObject {
    fun <T>withLock(block: ()->T): T
}

/**
 * Deprecated. We encourage switch to couroutines with rih set of synchronzation tools. This was created when
 * there was no coroutines support on native targets and coroutines library was new and not mature.
 *
 * The platform-dependent micro mutually-excelusive exection. WARNING! It does not (yet) works on native where
 * multithreading is actually not supported whatever jetbrains thinks (as for march 2022). So on native platform
 * it hust calls the block...
 */
@Deprecated("switch to coroutines")
expect fun MSyncObject(): MPSyncObject