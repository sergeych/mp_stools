@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("unused")

package net.sergeych.mp_tools

import kotlin.math.max

/**
 * Decode 4-byte signed integer at the specified offset in big-endian mode. Compatible with `ByteBuffer.getInt`
 * with default `BIG_ENDIAN` mode.
 */
fun ByteArray.getInt(at: Int): Int {
    var offset = at
    var result = 0

    result = (result shl 8) or this[offset++].toUByte().toInt()
    result = (result shl 8) or this[offset++].toUByte().toInt()
    result = (result shl 8) or this[offset++].toUByte().toInt()
    result = (result shl 8) or this[offset].toUByte().toInt()

    return result
}

/**
 * Encode and put 4-byte signed integer at the specified offset in big-endian mode. Compatible with `ByteBuffer.putInt`
 * with default `BIG_ENDIAN` mode.
 */
fun ByteArray.putInt(at: Int,value: Int) {
    var offset = at+3
    var x = value

    for( i in 0..3) {
        this[offset--] = (x and 0xFF).toByte()
        x = x shr 8
    }
}

/**
 * Find first occurrence of a binary substring in this binary array. Uses fast Boyer-Moore algorithm.
 */
fun ByteArray.indexOf(needle: ByteArray) = indexOf(toUByteArray(), needle.toUByteArray())

/**
 * Find first occurrence of a binary substring in this binary array. Uses fast Boyer-Moore algorithm.
 */
fun ByteArray.indexOf(needle: String) = indexOf(toUByteArray(), needle.encodeToByteArray().toUByteArray())

/**
 * Search the data array for the first occurrence of the
 * specified subarray and return its index or -1 if not found.
 *
 * There is no Galil because it only generates one match.
 *
 * @param haystack The data to be scanned
 * @param needle The string to search
 * @param offset offset to start search from
 * @return The start index of the substring
 */
private fun indexOf(haystack: UByteArray, needle: UByteArray,offset: UInt=0u): Int {
    if (needle.size == 0) {
        return 0
    }
    val charTable = makeCharTable(needle)
    val offsetTable = makeOffsetTable(needle)
    var i = needle.size - 1 + offset.toInt()
    var j: Int
    while (i < haystack.size) {
        j = needle.size - 1
        while (needle[j] == haystack[i]) {
            if (j == 0) {
                return i
            }
            --i
            --j
        }
        // i += needle.length - j; // For naive method
        i += max(offsetTable[needle.size - 1 - j], charTable[haystack[i].toInt()])
    }
    return -1
}

/**
 * Makes the jump table based on the mismatched character information.
 */
private fun makeCharTable(needle: UByteArray): IntArray {
    val ALPHABET_SIZE: Int = UByte.MAX_VALUE.toInt() + 1
    val table = IntArray(ALPHABET_SIZE)
    for (i in table.indices) {
        table[i] = needle.size
    }
    for (i in needle.indices) {
        table[needle[i].toInt()] = needle.size - 1 - i
    }
    return table
}

/**
 * Makes the jump table based on the scan offset which mismatch occurs.
 * (bad character rule).
 */
private fun makeOffsetTable(needle: UByteArray): IntArray {
    val table = IntArray(needle.size)
    var lastPrefixPosition = needle.size
    for (i in needle.size downTo 1) {
        if (isPrefix(needle, i)) {
            lastPrefixPosition = i
        }
        table[needle.size - i] = lastPrefixPosition - i + needle.size
    }
    for (i in 0 until needle.size - 1) {
        val slen = suffixLength(needle, i)
        table[slen] = needle.size - 1 - i + slen
    }
    return table
}

/**
 * Is needle[p:end] a prefix of needle?
 */
private fun isPrefix(needle: UByteArray, p: Int): Boolean {
    var i = p
    var j = 0
    while (i < needle.size) {
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
private fun suffixLength(needle: UByteArray, p: Int): Int {
    var len = 0
    var i = p
    var j = needle.size - 1
    while (i >= 0 && needle[i] == needle[j]) {
        len += 1
        --i
        --j
    }
    return len
}