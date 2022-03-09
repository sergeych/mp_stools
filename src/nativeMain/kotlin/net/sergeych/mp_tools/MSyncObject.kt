package net.sergeych.mp_tools

@Suppress("unused")
actual fun MSyncObject(): MPSyncObject = object : MPSyncObject {
    override fun <T> withLock(block: () -> T): T {
        // TODO: implement something when its ready
        return block()
    }
}