package mo_logger

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.sergeych.mptools.CachedExpression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class ToolsTestJVM {
    @Test
    fun cachedExpression() = runBlocking {
        val ce = CachedExpression<String>(20.milliseconds)
        assertNull(ce.cachedOrNull())
        assertEquals("foo", ce.get { "foo" })
        assertEquals("foo", ce.get { "bar" })
        delay(70)
        assertEquals("bar", ce.get { "bar" })
    }


}