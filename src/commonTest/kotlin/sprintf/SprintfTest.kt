package sprintf

import net.sergeych.sprintf.net.sergeych.mp_logger.DecimalSplitter
import net.sergeych.sprintf.net.sergeych.mp_logger.sprintf
import kotlin.test.*

internal class SprintfTest {

    @Test
    fun testSimpleFormat() {
        assertEquals("hello", "hello".sprintf())
        assertEquals("% percent", "%% percent".sprintf())
        assertEquals("10%", "10%%".sprintf())
    }

    @Test
    fun testIntegers() {
        assertEquals("== 3 ==","== %d ==".sprintf(3))
        assertEquals("==   3 ==","== %3d ==".sprintf(3))
        assertEquals("== 003 ==","== %03d ==".sprintf(3))
        assertEquals("== 3   ==","== %-3d ==".sprintf(3))
        assertEquals("==  3  ==","== %^3d ==".sprintf(3))
        assertEquals("== **3** ==","== %*^5d ==".sprintf(3))
        assertEquals("== __3__ ==","== %_^5d ==".sprintf(3))

        assertEquals("== +3 ==","== %+d ==".sprintf(3))
        assertEquals("==    +3 ==","== %+5d ==".sprintf(3))
        assertEquals("== +0003 ==","== %+05d ==".sprintf(3))

        assertEquals("== 1e ==","== %x ==".sprintf(0x1e))
        assertEquals("== 1E ==","== %X ==".sprintf(0x1e))

        assertEquals("== ###1e ==","== %#5x ==".sprintf(0x1e))
        assertEquals("== 1e### ==","== %#-5x ==".sprintf(0x1e))
        assertEquals("== ##1E## ==","== %#^6X ==".sprintf(0x1e))
    }

    @Test
    fun testDecimals() {
        var ds = DecimalSplitter(498023420.3241234123)
        assertEquals("4", ds.integer)
        assertEquals("980234203241234", ds.fraction)
        assertEquals("8", ds.exponent)
        ds = DecimalSplitter(54.329765)
        assertEquals("54", ds.integer)
        assertEquals("329765", ds.fraction)
        assertEquals("", ds.exponent)
        println(ds.format(-1, 4))
//        assertEquals("54.3298", ds.format(-1, 4))
    }
    @Test
    fun testStrings() {
        assertEquals("== 3 ==","== %s ==".sprintf(3))
        assertEquals("==   3 ==","== %3s ==".sprintf(3))
        assertEquals("== 3   ==","== %-3s ==".sprintf(3))
        assertEquals("==  3  ==","== %^3s ==".sprintf(3))
        assertEquals("== **3** ==","== %*^5s ==".sprintf(3))
        assertEquals("== __3__ ==","== %_^5s ==".sprintf(3))
    }
}