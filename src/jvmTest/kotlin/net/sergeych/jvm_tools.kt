package net.sergeych

import net.sergeych.mp_tools.getInt
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class TestBinaryInteroperability {
    @Test
    fun getInt() {
        val N = 256+4
        val bb = ByteBuffer.allocate(N)
        val bd = bb.array()
        for( i in 0 until N) {
            bb.array()[i] = i.toByte()
        }
        for( i in 0 ..N-4) {
            assertEquals(bb.getInt(i), bd.getInt(i))
        }

    }


}

