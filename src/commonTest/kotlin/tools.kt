import net.sergeych.mp_tools.decodeBase64
import net.sergeych.mp_tools.decodeBase64Compact
import net.sergeych.mp_tools.encodeToBase64
import net.sergeych.mp_tools.encodeToBase64Compact
import kotlin.random.Random
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

}