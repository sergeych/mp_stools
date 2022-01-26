package net.sergeych.mp_logger

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class LogFormatter {
    val lastDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val buffer = mutableListOf<String>("---- log started: $")

}