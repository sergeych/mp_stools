import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.sergeych.mp_tools.*
import net.sergeych.mptools.isInFuture
import net.sergeych.mptools.isInPast
import net.sergeych.mptools.withReentrantLock
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
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

        // this should not fail (multiline, spaces before and after:
        """
            
        JgAcAQABvID3cUi1Rk8XEdu+BSs2Kodi6kkd41LVM67i2uBwfQw08da+Ve5Vb/XVq095TLHSzugFliL4
        u57b4WEiNEDctWHSa441YZe+UO/VHvRkobKo87FZ6yWtp5YgduZ+YtFrAg6QVLEYw5pBUdoY7d84N49I
        myORomyn6JylYXUQv/Gob7yA52m9fiCKZaX01kUj6T9fiMDmI9KLbdJVJrfrlGaJOgXd1cQDGfwVmQhs
        1kMrTvZhMy4MNInySAPxfxsEBUM1n702lkO1mUz7s3vxaIjr6iGOInVJ9UXqGBRXTMpsg9+hsOfINAKj
        4OuND88Dwy5R31GMiReAt01Qlg57L1MzY2M=
        
        
        """.decodeBase64()
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun reentrantMutex() = runTest {
        val m = Mutex()
        var x = 0
        coroutineScope {
            for (i in 1..100) {
                launch {
                    m.withReentrantLock {
                        val t = x
                        delay(10)
                        m.withReentrantLock {  x = t + 1 }
                    }
                }
            }
        }
        assertEquals(100, x)
    }

    @Test
    fun timeTools() {
        val x = Clock.System.now()
        assertTrue { (x - 1.seconds).isInPast }
        assertTrue { (x + 10.seconds).isInFuture }
    }

//    @Test
//    fun testTime() = runTest {
//        val x1 = cur
//    }

}