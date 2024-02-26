package net.sergeych.sprintf

/**
 * Platform could provide current locale based month name or return null to use English. Month number is 1..12
 * as default in date operations in java
 */
actual fun LocaleSpecificMonthName(monthNumber: Int): String? {
    return null
}

actual fun LocaleSpecificAbbreviatedMonthName(monthNumber: Int): String? {
    return null
}

actual fun LocaleSpecificDayName(isoDayNumber: Int): String? {
    return null
}

actual fun LocaleSpecificAbbreviatedDayName(isoDayNumber: Int): String? {
    return null
}