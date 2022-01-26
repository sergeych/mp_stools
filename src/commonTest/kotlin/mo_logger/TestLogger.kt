import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.sergeych.mp_logger.Log
import net.sergeych.mp_logger.LogTag
import net.sergeych.mp_logger.Loggable
import net.sergeych.mp_logger.info
import kotlin.test.*

class TestLogger {
    // this code will not work on native (because of theri STUPID atomic-fu and all other shot0fu and
    // single-threadness-fu of underqualified system architects. Now looking for a way to limit tests to non-native
    // implementations
    @Test
    fun testConsole() = runTest {
        val x = object : Loggable by LogTag("TSTOB") {}
        try {
            x.info { "that should be missing" }
            Log.connectConsole()
            CoroutineScope(Dispatchers.Default).launch {
                    Log.logFlow.collect { z ->
                        println("*** ${z.data.message}")
                    }
            }
            x.info { "this one should be shown" }
            println("--- pre delay --- ${Clock.System.now()}")
            delay(3000)
            println("--- post delay ---${Clock.System.now()}")
        }
        finally {
            Log.disconnectConsole()
        }
    }
}