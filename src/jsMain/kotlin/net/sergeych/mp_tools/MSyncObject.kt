package net.sergeych.mp_tools

// in JS world there is no concurrency so it is simple
internal object DymmySync : MPSyncObject {
    override fun <T> withLock(block: () -> T): T = block()
}

@Suppress("unused")
actual fun MSyncObject(): MPSyncObject = DymmySync