package net.sergeych.mp_tools

import kotlin.math.max

/**
 * Fast search this string using Boyer-Moore algorithm adapted for Unicode characters.
 *
 * There is no Galil because it only generates one match.
 *
 * @param needle The string to search
 * @param offset offset to start search from
 * @return The start index of the substring or -1 if not found
 */
fun String.fastSearch(needle: String,offset: Int=0): Int {
    if (needle.isEmpty()) return 0

    val charTable = makeCharTable(needle)
    val offsetTable = makeOffsetTable(needle)
    var i = needle.length - 1 + offset
    var j: Int
    while (i < length) {
        j = needle.length - 1
        while (needle[j] == this[i]) {
            if (j == 0) {
                return i
            }
            --i
            --j
        }
        // i += needle.length - j; // For naive method
        i += max(offsetTable[needle.length - 1 - j], charTable[this[i].code])
    }
    return -1
}

/**
 * Makes the jump table based on the mismatched character information.
 */
private fun makeCharTable(needle: String): IntArray {
    val ALPHABET_SIZE: Int = UByte.MAX_VALUE.toInt() + 1
    val table = IntArray(ALPHABET_SIZE)
    for (i in table.indices) {
        table[i] = needle.length
    }
    for (i in needle.indices) {
        table[needle[i].code] = needle.length - 1 - i
    }
    return table
}

/**
 * Makes the jump table based on the scan offset which mismatch occurs.
 * (bad character rule).
 */
private fun makeOffsetTable(needle: String): IntArray {
    val table = IntArray(needle.length)
    var lastPrefixPosition = needle.length
    for (i in needle.length downTo 1) {
        if (isPrefix(needle, i)) {
            lastPrefixPosition = i
        }
        table[needle.length - i] = lastPrefixPosition - i + needle.length
    }
    for (i in 0 until needle.length - 1) {
        val slen = suffixLength(needle, i)
        table[slen] = needle.length - 1 - i + slen
    }
    return table
}

/**
 * Is needle[p:end] a prefix of needle?
 */
private fun isPrefix(needle: String, p: Int): Boolean {
    var i = p
    var j = 0
    while (i < needle.length) {
        if (needle[i] != needle[j]) {
            return false
        }
        ++i
        ++j
    }
    return true
}

/**
 * Returns the maximum length of the substring ends at p and is a suffix.
 * (good suffix rule)
 */
private fun suffixLength(needle: String, p: Int): Int {
    var len = 0
    var i = p
    var j = needle.length - 1
    while (i >= 0 && needle[i] == needle[j]) {
        len += 1
        --i
        --j
    }
    return len
}