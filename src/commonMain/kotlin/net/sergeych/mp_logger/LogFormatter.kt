package net.sergeych.mp_logger

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.sergeych.sprintf.sprintf

/**
 * Format [LogEntry] to set of string (multiline output) implementing log start and data change markup. This
 * implementation is human-readable, plain text, in machine-parseable form
 */
open class LogFormatter {

    /**
     * The separator that begins and ends separate line inserted in the beginning of the log
     */
    open val startDelimiter = "----------"

    /**
     * The separator that begins and ends separate line inserted when the date has been changed from the
     * last message. It is not inserter before the first entry.
     */
    open val dateChangleDelimiter = "---"

    /**
     * Default timezone captured when the formatter is created. Note that subsequend default time zone changes will
     * not affect created formatter instance that may be in some cases not a desired implementation. Override it to
     * refresh default timezone if need.
     */
    protected open val tz = TimeZone.currentSystemDefault()

    protected var lastDate: LocalDate? = null
        private set

    /**
     * Accumulates outout lines. Will be returned and cleared diring the next [format] call.
     */
    protected val buffer by lazy {
        mutableListOf<String>("%s log started: %tc %1!s".sprintf(startDelimiter, Clock.System.now()))
    }

    /**
     * Prepares a single entry to be shown. This version uses default formatting.
     */
    open protected fun formatEntry(le: LogEntry) = le.toString()

    /**
     * format log entry to one or more strings to be emitted to the log console, file, etc.
     * @param le entry to be formatted
     * @return array (empty or with 1+ strings) to be displayed.
     */
    open fun format(le: LogEntry): List<String> {
        val now = Clock.System.now().toLocalDateTime(tz).date
        if (lastDate == null) lastDate = now
        else {
            if(now != lastDate) {
                buffer.add("%s Date changed: %s %1!s".sprintf(dateChangleDelimiter, now))
                lastDate = now
            }
        }
        buffer.add(formatEntry(le))
        return buffer.toList().also { buffer.clear() }
    }

}