package net.sergeych.sprintf

/**
 * Platform could provide current locale based month name or return null to use English
 */
actual fun LocaleSpecificMonthName(monthNumber: Int): String? {
    // TODO extract local month names
    return null
}

actual fun LocaleSpecificAbbreviatedMonthName(monthNumber: Int): String? {
    // TODO extract local month name abbreviations
    return null
}

actual fun LocaleSpecificDayName(isoDayNumber: Int): String? {
    return null
}

actual fun LocaleSpecificAbbreviatedDayName(isoDayNumber: Int): String? {
    return null
}