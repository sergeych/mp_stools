package sprintf

import net.sergeych.sprintf.ExponentFormatter
import net.sergeych.sprintf.fractionalFormat
import net.sergeych.sprintf.net.sergeych.mp_logger.sprintf
import net.sergeych.sprintf.scientificFormat
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SprintfTest {

    @Test
    fun testSimpleFormat() {
        assertEquals("hello", "hello".sprintf())
        assertEquals("% percent", "%% percent".sprintf())
        assertEquals("10%", "10%%".sprintf())
    }

    @Test
    fun testIntegers() {
        assertEquals("1 2 3", "%d %d %d".sprintf(1,2,3))

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
        assertEquals("01 ff", "%02x %02x".sprintf(1, 255))

        assertEquals("== ###1e ==","== %#5x ==".sprintf(0x1e))
        assertEquals("== 1e### ==","== %#-5x ==".sprintf(0x1e))
        assertEquals("== ##1E## ==","== %#^6X ==".sprintf(0x1e))
    }

    @Test
    fun testFractionedDecimals() {
        assertEquals("0.124", fractionalFormat(0.1237, -1, 3))
        assertEquals("0.1237", fractionalFormat(0.1237, -1, 4))
        assertEquals("0.12370", fractionalFormat(0.1237, -1, 5))
        assertEquals("0.123700", fractionalFormat(0.1237, -1, 6))
        assertEquals("0.1", fractionalFormat(0.1237, -1, 1))
        assertEquals("0.12", fractionalFormat(0.1237, 4, -1))
        assertEquals("-0.1", fractionalFormat(-0.1237, 4, -1))

        assertEquals("-0.124", fractionalFormat(-0.1237, -1, 3))
        assertEquals("-0.1237", fractionalFormat(-0.1237, -1, 4))

        assertEquals("-221.12", fractionalFormat(-221.1217, -1, 2))
        assertEquals("-221.122", fractionalFormat(-221.1217, -1, 3))

        assertEquals("221.122", fractionalFormat(221.1217, -1, 3))
        assertEquals("221.1217", fractionalFormat(221.1217, -1, 4))

        assertEquals("221.122", "%.3f".sprintf(221.1217))
        assertEquals("__221.1", "%_7.1f".sprintf(221.1217))
        assertEquals("_+221.1", "%+_7.1f".sprintf(221.1217))
        assertEquals("+0221.1", "%+07.1f".sprintf(221.1217))
        assertEquals("00221.1", "%07.1f".sprintf(221.1217))

        assertEquals("1.000", "%.3f".sprintf(1))
    }

    @Test
    fun testScientificDecimals() {
        val x = ExponentFormatter(-162.345678)
        println(":: $x")
//        for( i in 3 .. 15 ) println("${x.value} $i: ${x.scientific(i)}")
        fun test(n: Int,expected: String) {
            assertEquals(expected, x.scientific(n))
        }
        test(3, "-2e2")
        test(4, "-2e2")
        test(5, "-2.e2")
        test(6, "-1.6e2")
        test(7, "-1.62e2")
        test(8, "-1.623e2")
        test(9, "-1.6235e2")
        test(10, "-1.62346e2")
        test(11, "-1.623457e2")
        test(12, "-1.6234568e2")
        test(13, "-1.62345678e2")
        test(14, "-1.62345678e2")
        test(15, "-1.62345678e2")

        assertEquals("2.4e0", scientificFormat(2.39, 5) )
        assertEquals("-2.4e-3", scientificFormat(-2.39e-3, 7) )
        assertEquals("2.4e-3", scientificFormat(2.39e-3, 6) )

        assertEquals("-2.4e-3", scientificFormat(-2.39e-3, -1, 1) )
        assertEquals("-2.39e-3", scientificFormat(-2.39e-3, -1, 2) )

        assertEquals("-2.39e-3", "%.2e".sprintf(-2.39e-3, -1, 2) )
        assertEquals("2.4e-3", "%6e".sprintf(2.39e-3))

        assertEquals("-2.39E-3", "%.2E".sprintf(-2.39e-3) )
        assertEquals("2.39E-3", "%.2E".sprintf(2.39e-3) )
        assertEquals("+2.39E-3", "%+.2E".sprintf(2.39e-3) )

        assertEquals("2.4E-3", "%6E".sprintf(2.39e-3))
        assertEquals("0002.4E-3", "%09.1E".sprintf(2.39e-3))
        assertEquals("+002.4E-3", "%+09.1E".sprintf(2.39e-3))
    }

    @Test
    fun testAutoFloats() {
        assertEquals("17.234", "%g".sprintf(17.234))
        assertEquals("**17.234", "%*8g".sprintf(17.234))
        assertEquals("+017.234", "%+08g".sprintf(17.234))
    }

    @Test
    fun testStrings() {
        assertEquals("== 3 ==","== %s ==".sprintf(3))
        assertEquals("==   3 ==","== %3s ==".sprintf(3))
        assertEquals("== 3   ==","== %-3s ==".sprintf(3))
        assertEquals("==  3  ==","== %^3s ==".sprintf(3))
        assertEquals("== **3** ==","== %*^5s ==".sprintf(3))
        assertEquals("== __3__ ==","== %_^5s ==".sprintf(3))
        assertEquals("*****hello!","%*10s!".sprintf("hello"))
        assertEquals("Hello, world!","%s, %s!".sprintf("Hello", "world"))
        assertEquals("___centered___","%^_14s".sprintf("centered"))
    }

    @Test
    fun testCharacters() {
        assertEquals("Cat!", "Ca%c!".sprintf('t'))
        assertEquals("Cat!", "Ca%C!".sprintf('t'))
    }

    @Test
    fun testOctals() {
        assertEquals("7 10", "%o %o".sprintf(7,8))
        assertEquals("007 010", "%03o %03o".sprintf(7,8))
    }
}