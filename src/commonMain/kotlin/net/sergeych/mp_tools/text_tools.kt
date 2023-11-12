package net.sergeych.mp_tools

/**
 * If the string is longer that given, replace it's middle part with a unicode ellipsis
 * character so the overall length will be [size]
 */
fun String.trimMiddle(size: Int): String {
    if (this.length <= size) return this
    var l0 = (size - 1) / 2
    val l1 = l0
    if (l0 + l1 + 1 < size) l0++
    if (l0 + l1 + 1 != size) throw RuntimeException("big in trimMiddle: $size $l0 $l1")
    return substring(0, l0) + '…' + substring(length - l1)
}

/**
 * Trim this string as needed and append ellipsis character so the resulting size will be
 * no longer than [size]
 */
fun String.trimToEllipsis(size: Int): String {
    if (this.length <= size) return this
    return (this.substring(0, size - 1)) + '…'
}

/**
 * Convert number to human-readable estimated value in b, Kb, Mb, Gb amd Pb
 * to make the nuymber comfortably readable
 */
@Suppress("unused")
fun Number.toDataSize(): String {
    var d = toLong()
    if (d < 1024)
        return "${d}b"
    d /= 1024
    if (d < 1024)
        return "${d}Kb"
    d /= 1024
    if (d < 1024)
        return "${d}Mb"
    d /= 1024
    if (d < 1024)
        return "${d}Gb"
    d /= 1024
    if (d < 1024)
        return "${d}Tb"
    d /= 1024
    return "${d}Pb"
}

/**
 * Format any number type by separating thousands, millions, etc in _integer part_ using
 * a space or other [separator].
 */
fun Number.withThousandsSeparator(separator: String=" "): String {
    val src = toString()
    val result = StringBuilder()
    var pos = src.indexOf('.')
    if( pos >= 0 ) {
        result.append(src.substring(pos--))
    }
    else pos = src.lastIndex
    var count = 0

    while(pos >= 0) {
        if( src[pos] == '-') {
            result.insert(0, '-')
            pos--
        }
        else {
            if (count++ == 3) {
                count = 1
                result.insert(0, separator)
            }
            result.insert(0, src[pos--])
        }
    }
    return result.toString()
}