package net.sergeych.mp_logger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.sergeych.mp_tools.AsyncBouncer
import net.sergeych.mp_tools.globalLaunch
import net.sergeych.mptools.Now
import net.sergeych.sprintf.sprintf
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.time.Duration.Companion.milliseconds

class FileLogCatcher(name: String, level: Log.Level = Log.Level.DEBUG, rotate: Boolean = false) {

    private val currentLog: File

//    private var queue = LinkedBlockingQueue<Logger.Entry>()

    /**
     * Export current logs to zip, with optional metadata. It exports current
     * log as `current.log` and, if exists, `previous.log`.
     *
     * @param metadata if present, will be put as `metadata.txt` file
     * @param output if present, zip wile will be written to it (Existing file will be deleted).
     *          Otherwise new temp wile in delete-on-exit mode will be created.
     * @return ready zip file
     */
    @Suppress("unused")
    fun exportZip(metadata: String? = null, output: File? = null): File {
        val f = if (output != null) {
            if (output.exists()) output.delete()
            output
        } else {
            val f = File.createTempFile("exprort", "log")
            f.deleteOnExit()
            f
        }
        val zos = ZipOutputStream(f.outputStream())
        zos.setLevel(9)
        if (metadata != null) {
            zos.putNextEntry(ZipEntry("metadata.txt"))
            zos.write(metadata.encodeToByteArray())
        }
        zos.putNextEntry(ZipEntry("current.log"))
        zos.write(currentLog.readBytes())
        zos.closeEntry()
        zos.close()
        return f
    }

    val job: Job
    private val out: BufferedWriter
    private val bouncer: AsyncBouncer

    suspend fun close() {
        try {
            if (!bouncer.isClosed) {
                bouncer.close()
                job.cancel()
                withContext(Dispatchers.IO) {
                    out.close()
                }
            }
        } catch (x: Throwable) {
            x.printStackTrace()
        }
    }

    suspend fun flush() {
        bouncer.pulse(true)
        withContext(Dispatchers.IO) {
            out.flush()
        }
    }

    init {
        currentLog = File(name)
        currentLog.parentFile?.let { root ->
            if (!root.exists()) root.mkdirs()
        }

        val lf = LogFormatter()
        val stream = if (currentLog.exists()) {
            if (rotate) {
                val rotated = File("%s_%t#.gz".sprintf(name, Now()))
                val gz = GZIPOutputStream(rotated.outputStream())
                currentLog.inputStream().use { it.copyTo(gz) }
                gz.close()
                currentLog.outputStream()
            } else {
                FileOutputStream(currentLog, true)
            }
        } else
            currentLog.outputStream()
        out = stream.bufferedWriter()

        println("writing to $currentLog")

        bouncer = AsyncBouncer(100.milliseconds) { withContext(Dispatchers.IO) { out.flush() } }

        job = globalLaunch {
            Log.logFlow.collect {
                if (it.level >= level)
                    bouncer.performAndPulse {
                        for (line in lf.format(it)) {
                            withContext(Dispatchers.IO) {
                                out.write(line)
                                out.newLine()
                            }
                        }
                    }
            }
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking { close() }
        })

    }
}