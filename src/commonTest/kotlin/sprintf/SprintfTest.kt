package sprintf

import kotlinx.datetime.*
import net.sergeych.sprintf.ExponentFormatter
import net.sergeych.sprintf.fractionalFormat
import net.sergeych.sprintf.scientificFormat
import net.sergeych.sprintf.sprintf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
//        println(":: $x")
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
    fun testSientificFormat() {
        val x = ExponentFormatter(3.349832285740512)
        assertEquals("3.350e0", x.scientific(7))

        assertEquals("3.350e0", x.scientific(7))

        assertEquals("9.998e0", ExponentFormatter(9.9980).scientific(7))
        assertEquals("9.998e0", ExponentFormatter(9.9981).scientific(7))
        assertEquals("9.998e0", ExponentFormatter(9.9982).scientific(7))
        assertEquals("9.998e0", ExponentFormatter(9.9983).scientific(7))
        assertEquals("9.998e0", ExponentFormatter(9.9984).scientific(7))
        assertEquals("9.999e0", ExponentFormatter(9.9985).scientific(7))
        assertEquals("9.999e0", ExponentFormatter(9.9986).scientific(7))
        assertEquals("9.999e0", ExponentFormatter(9.9987).scientific(7))
        assertEquals("9.999e0", ExponentFormatter(9.9988).scientific(7))
        assertEquals("9.999e0", ExponentFormatter(9.9989).scientific(7))

        assertEquals("9.939e0", ExponentFormatter(9.9390).scientific(7))
        assertEquals("9.939e0", ExponentFormatter(9.9391).scientific(7))
        assertEquals("9.939e0", ExponentFormatter(9.9392).scientific(7))
        assertEquals("9.939e0", ExponentFormatter(9.9393).scientific(7))
        assertEquals("9.939e0", ExponentFormatter(9.9394).scientific(7))
        assertEquals("9.940e0", ExponentFormatter(9.9395).scientific(7))
        assertEquals("9.940e0", ExponentFormatter(9.9396).scientific(7))
        assertEquals("9.940e0", ExponentFormatter(9.9397).scientific(7))
        assertEquals("9.940e0", ExponentFormatter(9.9398).scientific(7))
        assertEquals("9.940e0", ExponentFormatter(9.9399).scientific(7))

        assertEquals("9.399e0", ExponentFormatter(9.3990).scientific(7))
        assertEquals("9.399e0", ExponentFormatter(9.3991).scientific(7))
        assertEquals("9.399e0", ExponentFormatter(9.3992).scientific(7))
        assertEquals("9.399e0", ExponentFormatter(9.3993).scientific(7))
        assertEquals("9.399e0", ExponentFormatter(9.3994).scientific(7))
        assertEquals("9.400e0", ExponentFormatter(9.3995).scientific(7))
        assertEquals("9.400e0", ExponentFormatter(9.3995).scientific(7))
        assertEquals("9.400e0", ExponentFormatter(9.3996).scientific(7))
        assertEquals("9.400e0", ExponentFormatter(9.3997).scientific(7))
        assertEquals("9.400e0", ExponentFormatter(9.3998).scientific(7))
        assertEquals("9.400e0", ExponentFormatter(9.3999).scientific(7))

        assertEquals("9.999e0", ExponentFormatter(9.9990).scientific(7))
        assertEquals("9.999e0", ExponentFormatter(9.9991).scientific(7))
        assertEquals("9.999e0", ExponentFormatter(9.9992).scientific(7))
        assertEquals("9.999e0", ExponentFormatter(9.9993).scientific(7))
        assertEquals("9.999e0", ExponentFormatter(9.9994).scientific(7))
        assertEquals("1.000e1", ExponentFormatter(9.9995).scientific(7))
        assertEquals("1.000e1", ExponentFormatter(9.9996).scientific(7))
        assertEquals("1.000e1", ExponentFormatter(9.9995).scientific(7))
        assertEquals("1.000e1", ExponentFormatter(9.9998).scientific(7))
        assertEquals("1.000e1", ExponentFormatter(9.9999).scientific(7))

        assertEquals("3.350e0", x.scientific(7))


        assertEquals("3.350e0", "%.3e".sprintf(3.349832285740512 ))
        assertEquals("1.000e1", "%.3e".sprintf(9.9999 ))

        assertEquals("1.000e1", "%.3e".sprintf(9.9999 ))
    }

    @Test
    fun testFractionalFormat() {
        assertEquals("3.340", "%.3f".sprintf(3.34011 ))
        assertEquals("3.340", "%.3f".sprintf(3.34022 ))
        assertEquals("3.340", "%.3f".sprintf(3.34033 ))
        assertEquals("3.340", "%.3f".sprintf(3.34044 ))
        assertEquals("3.341", "%.3f".sprintf(3.34054 ))
        assertEquals("3.341", "%.3f".sprintf(3.34065 ))
        assertEquals("3.341", "%.3f".sprintf(3.34076 ))
        assertEquals("3.341", "%.3f".sprintf(3.34087 ))
        assertEquals("3.341", "%.3f".sprintf(3.34098 ))

        assertEquals("3.399", "%.3f".sprintf(3.39911 ))
        assertEquals("3.399", "%.3f".sprintf(3.39921 ))
        assertEquals("3.399", "%.3f".sprintf(3.39931 ))
        assertEquals("3.399", "%.3f".sprintf(3.39942 ))

        assertEquals("3.400", "%.3f".sprintf(3.39951 ))
        assertEquals("3.400", "%.3f".sprintf(3.39961 ))
        assertEquals("3.400", "%.3f".sprintf(3.39971 ))
        assertEquals("3.400", "%.3f".sprintf(3.39981 ))
        assertEquals("3.400", "%.3f".sprintf(3.39991 ))

        assertEquals("10.0", "%.1f".sprintf(9.99991 ))

        // older tests
//        assertEquals("3.399", "%.3f".sprintf(3.390 ))
//        assertEquals("3.390", "%.3f".sprintf(3.395 ))
//        assertEquals("3.340", "%.3f".sprintf(3.3405 ))
//        assertEquals("3.350", "%.3f".sprintf(3.349832285740512 ))
//        assertEquals("3.350", "%.3f".sprintf(3.349832285740512 ))
//        assertEquals("3.350", "%.3f".sprintf(3.349832285740512 ))

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

    @Test
    fun testTime() {
//        val t = Clock.System.now()
        val t = LocalDateTime(1970, 5, 6, 5, 45, 11, 123456789 )
//        println("%tH:%tM:%tS.%tL (%tN)".sprintf(t, t, t, t, t, t))
        assertEquals("05:45:11.123 (123456789)","%1\$tH:%1\$tM:%1\$tS.%1\$tL (%1\$tN)".sprintf(t))
        assertEquals("05:45:11.123 (123456789)","%1!tH:%1!tM:%1!tS.%1!tL (%1!tN)".sprintf(t))

        assertEquals("May 6, 1970","%1!tB %1!te, %1!tY".sprintf(t))
        assertEquals("06.05.1970","%1!td.%1!tm.%1!tY".sprintf(t))
        assertEquals("06.05.70","%1!td.%1!tm.%1!ty".sprintf(t))
        assertEquals("06.May.70","%1!td.%1!th.%1!ty".sprintf(t))
        assertEquals("06.May.70","%1!td.%1!tB.%1!ty".sprintf(t))
        assertEquals("Day 126, it was Wednesday.","Day %tj, it was %1!tA.".sprintf(t))
        assertEquals("Day 126, it was Wed.","Day %tj, it was %1!ta.".sprintf(t))

        assertEquals("05:45","%tR".sprintf(t))
        assertEquals("05:45:11","%tT".sprintf(t))

        val t1 = LocalDateTime(1970, 5, 6, 15, 45, 11, 123456789 )
        assertEquals("05:45:11 AM","%Tr".sprintf(t))
        assertEquals("03:45:11 pm","%tr".sprintf(t1))

        assertEquals("05/06/70","%tD".sprintf(t))
        assertEquals("1970-05-06","%tF".sprintf(t))
        // no idea how to test it without time zone dependency
//        assertEquals("Wed May 06 05:45:11 +01:00 1970","%tc".sprintf(t.toInstant(TimeZone.currentSystemDefault())))

        assertTrue { "%tO".sprintf(t).startsWith("1970-05-06T05:45:11") }
    }
}