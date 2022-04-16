@file:OptIn(ExperimentalCoroutinesApi::class)

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.sergeych.mp_logger.*
import net.sergeych.mptools.Now
import net.sergeych.sprintf.sprintf
import kotlin.test.Test


//class TA: Loggable by LogTag("TA") {
//
//}
//
//class TB: Loggable by LogTag("TB") {
//
//}

class TestLogger {
    // this code will not work on native (because of theri STUPID atomic-fu and all other shot0fu and
    // single-threadness-fu of underqualified system architects. Now looking for a way to limit tests to non-native
    // implementations
    @Test
    fun testConsole() = runBlocking {
        val x = object : Loggable by LogTag("TSTOB") {}
        try {
            x.info { "that should not be missing (replay)" }
            Log.connectConsole()
            x.info { "this one should be shown" }
            println("--- pre delay --- ${Clock.System.now()}")
            delay(30)
            println("--- post delay ---${Clock.System.now()}")
        }
        finally {
            Log.disconnectConsole()
        }
    }

    @Test
    fun testShutdownHook() = runTest {
        Log.connectConsole(Log.Level.DEBUG)
        val x = LogTag("test")
        x.debug { "Debug" }
        x.info { "Info" }
    }

    @Test
    fun fileTest() {
        runBlocking {
            Log.connectConsole(Log.Level.DEBUG)
            println("%t#".sprintf(Now()))
//            val b = AsyncBouncer(100.milliseconds) {
//                println("test bouncer")
//            }
//            b.pulse(true)
            val c = FileLogCatcher("testlog",rotate = true)
            val x = LogTag("TFILE")
            delay(300)
            for(i in 1..20) {
                x.info { "--------- we run ------------" }
                delay(50)
            }
            delay(4000)

        }
    }

//    @Test
//    fun levels() {
//        runBlocking {
//            Log.connectConsole()
//
//            val ta = TA()
//            val tb = TB()
//
//            fun d(text: String) {
//                ta.debug { text }; tb.debug { text }
//            }
//
//            fun i(text: String) {
//                ta.info { text }; tb.info { text }
//            }
//
//            d("m1")
//            ta.logLevel = Log.Level.DEBUG
//            d("m2")
//            d("m3")
//            i("m4")
//
////            val list = Log.log.
//            delay(1200)
////            println("-- log -- ${list.size}")
////            for (l in list) println("--> $l")
////            assertEquals(3, list.size)
//        }
//    }
}