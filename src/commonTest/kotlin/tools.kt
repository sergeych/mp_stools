import net.sergeych.mp_tools.*
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.*

class TestTools {

    @Test
    fun testBase64() {
        var src = byteArrayOf(1,3,4,4)
        assertEquals(src.encodeToBase64Compact(), "AQMEBA")
        assertEquals(src.encodeToBase64(), "AQMEBA==")
        assertContentEquals(src, "AQMEBA".decodeBase64Compact())
        assertContentEquals(src, "AQMEBA==".decodeBase64())

        for( i in 0..10) {
            for (s in 1..117) {
                src = Random.Default.nextBytes(s)
                var x = src.encodeToBase64()
                assertContentEquals(src, x.decodeBase64())
                x = src.encodeToBase64Compact()
                assertContentEquals(src, x.decodeBase64Compact())
            }
        }
    }

    @Test
    fun putInt() {
        val N = 256+4
        val bd = ByteArray(N)

        fun t(value:Int) {
            val index = Random.nextInt(0..N-4)
            bd.putInt(index, value)
            assertEquals(value, bd.getInt(index))
        }

        for( i in 0..1000) {
            val x = Random.nextInt(1, Int.MAX_VALUE)
            t(x)
        }
    }

    @Test
    fun searchInByteArray() {
        val offset = 1171
        val needle = "Fake vaccine kills".encodeToByteArray()
        val haystack = Random.nextBytes(offset) + needle + Random.nextBytes(offset/3)
        assertEquals(offset, haystack.indexOf(needle))
    }
}