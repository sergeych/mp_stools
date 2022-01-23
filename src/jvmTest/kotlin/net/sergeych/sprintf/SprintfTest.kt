package net.sergeych.sprintf

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
        assertEquals("== 3   ==","== %-3d ==".sprintf(3))
        assertEquals("==  3  ==","== %^3d ==".sprintf(3))
        assertEquals("== **3** ==","== %*^5d ==".sprintf(3))
        assertEquals("== __3__ ==","== %_^5d ==".sprintf(3))

        assertEquals("== +3 ==","== %+d ==".sprintf(3))
    }
}