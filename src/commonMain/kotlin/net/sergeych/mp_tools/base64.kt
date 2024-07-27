package net.sergeych.mp_tools

/*

Why reinventing the wheel?

Just because. There is no "good" and "safe" way to do in in browser without importing external libraries or going
async. Because there is no kotlin native version, and getting external native dependencies is what we aere trying
to avoid by all costs. And, finally, if we have to write pure kotlin implementation for js + native, we'd better
use it on JVM too - it is at least not worse than JVM standard library. So here we go, the wheel again ;)

Though the algorithm is old and very well known and optimized, this version is the kotlin adoption of
https://gist.github.com/enepomnyaschih/72c423f727d395eeaa09697058238727,
big thanks to [Egor](https://gist.github.com/enepomnyaschih).

 */
private val base64codes = arrayOf(
    255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 62, 255, 255, 255, 63,
    52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 255, 255, 255, 0, 255, 255,
    255, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
    15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 255, 255, 255, 255, 255,
    255, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
    41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
)

private val base64abc = arrayOf(
    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
    "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
    "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
    "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+", "/"
)

private fun getBase64Code(charCode: Int): Int {
    if (charCode >= base64codes.size) {
        throw Exception("Unable to parse base64 string.")
    }
    val code = base64codes[charCode]
    if (code == 255) {
        throw Exception("Unable to parse base64 string.")
    }
    return code
}

fun ByteArray.encodeToBase64(): String {
    val result = StringBuilder()
    val l = size

    fun b(index: Int) = this[index].toInt() and 0xFF

    var i = 2
    while (i < l) {
        result.append(base64abc[b(i - 2) shr 2])
        result.append(base64abc[((b(i - 2) and 0x03) shl 4) or (b(i - 1) shr 4)])
        result.append(base64abc[((b(i - 1) and 0x0F) shl 2) or (b(i) shr 6)])
        result.append(base64abc[b(i) and 0x3F])
        i += 3
    }

    if (i == l + 1) { // 1 octet yet to write
        result.append(base64abc[b(i - 2) shr 2])
        result.append(base64abc[(b(i - 2) and 0x03) shl 4])
        result.append("==")
    }
    if (i == l) { // 2 octets yet to write
        result.append(base64abc[b(i - 2) shr 2])
        result.append(base64abc[((b(i - 2) and 0x03) shl 4) or (b(i - 1) shr 4)])
        result.append(base64abc[(b(i - 1) and 0x0F) shl 2])
        result.append("=")
    }
    return result.toString()
}

private fun removeAllSpaces(src: String): String {
    val result = StringBuilder()
    for( ch in src) {
        when(ch) {
            ' ', '\t', '\n', '\r' -> continue
            else -> result.append(ch)
        }
    }
    return result.toString()
}
fun String.decodeBase64(): ByteArray {
    val str = removeAllSpaces(this)
    if (str.length % 4 != 0) {
        throw IllegalArgumentException("Unable to parse base64 string: wrong size")
    }
    val index = str.indexOf("=")
    if (index != -1 && index < str.length - 2) {
        throw IllegalArgumentException("Unable to parse base64 string: illegal characters")
    }

    val missingOctets = when {
        str.endsWith("==") -> 2
        str.endsWith("=") -> 1
        else -> 0
    }

    val result = ByteArray(3 * (str.length / 4))

    var i = 0
    var j = 0
    while (i < str.length) {
        val buffer: Int = (getBase64Code(str[i].code) shl 18) or
                (getBase64Code(str[i + 1].code) shl 12) or
                (getBase64Code(str[i + 2].code) shl 6) or
                getBase64Code(str[i + 3].code)
        result[j] = (buffer shr 16).and(0xFF).toByte()
        result[j + 1] = ((buffer shr 8) and 0xFF).toByte()
        result[j + 2] = (buffer and 0xFF).toByte()
        i += 4
        j += 3
    }
    return result.sliceArray(0 until result.size - missingOctets)
}

private val reSpaces = Regex("\\s+")

/**
 * Decode compact representation of base64. e.g. with oissibly no trailing '=' fill characters, for example,
 * encoded with [ByteArray.encodeToBase64Compact] fun.
 */
fun String.decodeBase64Compact(): ByteArray {
    val x = StringBuilder(reSpaces.replace(this, ""))
    while( x.length % 4 != 0 ) x.append('=')
    return x.toString().decodeBase64()
}

/**
 * Encode to base64 with no spaces and no trailing '=' fill characters, to be decoded with [String.decodeBase64Compact].
 */
fun ByteArray.encodeToBase64Compact(): String {
    val result = encodeToBase64()
    var end = result.length-1
    while( end > 0 && result[end] == '=') end--
    return result.slice(0..end)
}

/**
 * Url-friendly encoding, as used by Google, Yahoo (the name Y64), etc. [encodeToBase64Compact]
 * and substitute `+/` to `-_` respectively.
 */
fun ByteArray.encodeToBase64Url(): String =
    encodeToBase64Compact().replace('+','-').replace('/', '_')

/**
 * Decode base64 url encoded binary data. See [encodeToBase64Url] for more information
 */
@Suppress("unused")
fun String.decodeBase64Url(): ByteArray =
    replace('-','+').replace('_', '/').decodeBase64Compact()