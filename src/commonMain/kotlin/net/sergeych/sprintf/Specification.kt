package net.sergeych.sprintf

import net.sergeych.sprintf.net.sergeych.mp_logger.Sprintf

internal enum class Positioning {
    LEFT, RIGHT, CENTER
}

internal class Specification(val parent: Sprintf, var index: Int) {
    private var size: Int = -1
    private var positioninig = Positioning.RIGHT
    private var fillChar = ' '
    private var currentPart = StringBuilder()
    private var pos = 0
    private var explicitPlus = false
    private var done = false

    init {
        while (!done) {
            val ch = parent.nextChar()
            println("spec: $ch: [$currentPart]")
            when (ch) {
                '-', '^' -> {
                    if (currentPart.isNotEmpty()) parent.invalidFormat("unexpected $ch")
                    positioninig = if (ch == '-') Positioning.LEFT else Positioning.CENTER
                }
                '+' -> {
                    if (currentPart.isNotEmpty()) parent.invalidFormat("unexpected $ch")
                    explicitPlus = true
                }
                in "*#_=" -> {
                    if (!isStart) parent.invalidFormat("bad fill char $ch position")
                    fillChar = ch
                }
                '0' -> {
                    if (currentPart.isEmpty()) fillChar = '0'
                    else currentPart.append(ch)
                }
                in "0123456789" -> {
                    currentPart.append(ch)
                }
                's' -> createStringField()
                'd', 'i' -> createIntegerField()
                'x' -> createHexField(false)
                'X' -> createHexField(true)
                else -> parent.invalidFormat("unexpected character '$ch'")
            }
            pos++
        }
    }

    private val isStart: Boolean get() = pos == 0

    private fun createStringField() {
        done = true
        parseSize()
        insertField(parent.getText(index))
    }

    private fun createIntegerField() {
        done = true
        parseSize()
        val number = parent.getNumber(index).toLong()
        if( explicitPlus && fillChar == '0' && number > 0 )
            insertField(number.toString(), "+")
        else
            insertField(if( explicitPlus ) "+$number" else "$number")
    }

    private fun createHexField(upperCase: Boolean) {
        done = true
        parseSize()
        val number = parent.getNumber(index).toLong()
        if (explicitPlus) parent.invalidFormat("'+' is incompatible with hex format")
        val text = number.toString(16)
        insertField(if (upperCase) text.uppercase() else text.lowercase())
    }

    private fun parseSize() {
        if (currentPart.isNotEmpty()) {
            size = currentPart.toString().toInt()
            currentPart.clear()
        }
    }

    private fun insertField(text: String, prefix: String = "") {
        val l = text.length + prefix.length
        if (size < 0 || size < l) {
            parent.specificationDone(prefix + text)
        } else {
            var padStart = 0
            var padEnd = 0
            when (positioninig) {
                Positioning.LEFT -> padEnd = size - l
                Positioning.RIGHT -> padStart = size - l
                Positioning.CENTER -> {
                    padStart = (size - l) / 2
                    padEnd = size - padStart - l

                }
            }
            val result = StringBuilder(prefix)
            while (padStart-- > 0) result.append(fillChar)
            result.append(text)
            while (padEnd-- > 0) result.append(fillChar)
            parent.specificationDone(result.toString())
        }
    }
}