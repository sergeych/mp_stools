package net.sergeych.sprintf.net.sergeych.mp_logger

import kotlinx.datetime.*
import net.sergeych.sprintf.Specification

class Sprintf(val format: String, val args: Array<out Any?>) {

    private var pos = 0
    private var specStart = -1
    private val result = StringBuilder()
    private var currentIndex = 0

    fun process(): Sprintf {
        while (pos < format.length) {
            val ch = format[pos++]
//            println("$ch $pos $specStart [$result]")
            if (ch == '%') {
                when {
                    specStart == pos - 1 -> {
                        result.append(ch)
                        specStart = -1
                    }
                    specStart < 0 -> specStart = pos
                    else -> invalidFormat("unexpected %")
                }
            } else {
                if (specStart >= 0) {
                    pos--
                    Specification(this, currentIndex++).scan()
                } else result.append(ch)
            }
        }
        return this
    }

    internal fun nextChar(): Char {
        if (pos >= format.length) invalidFormat("unexpected end of string inside format specification")
        return format[pos++]
    }

    internal fun invalidFormat(reason: String): Nothing {
        throw IllegalArgumentException("bad format: $reason at ofset ${pos - 1} of \"$format\"")
    }

    override fun toString(): String = result.toString()

    internal fun getNumber(index: Int): Number {
        return notNullArg(index)
    }

    internal fun getText(index: Int): String {
        return args[index]!!.toString()
    }

    internal fun getCharacter(index: Int): Char {
        return notNullArg(index)
    }

    internal fun specificationDone(text: String) {
        result.append(text)
        specStart = -1
    }

    fun getLocalDateTime(index: Int): LocalDateTime {
        val t = notNullArg<Any>(index)
        return when(t) {
            is Instant -> t.toLocalDateTime(TimeZone.currentSystemDefault())
            is LocalDateTime -> t
            is LocalDate -> t.atTime(0,0,0)
            else -> ConvertToInstant(t).toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T>notNullArg(index: Int) = args[index]!! as T

    fun pushbackArgumentIndex() {
        currentIndex++
    }
}

fun String.sprintf(vararg args: Any?): String = Sprintf(this, args).process().toString()

fun String.format(vararg args: Any?): String = Sprintf(this, args).process().toString()

expect fun ConvertToInstant(t: Any): Instant