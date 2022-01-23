package net.sergeych.sprintf

import net.sergeych.sprintf.net.sergeych.mp_logger.Sprintf

internal enum class Positioning {
    LEFT, RIGHT, CENTER
}

internal class Specification(val parent: Sprintf, var index: Int) {
    private var size: Int = -1
    private var positioninig = Positioning.RIGHT
    private var showPlus = false
    private var fillChar = ' '
    private var currentPart = StringBuilder()
    private var pos = 0


    init {
        var done = false
        while (true) {
            val ch = parent.nextChar()
            println("spec: $ch: [$currentPart]")
            when (ch) {
                '-', '^' -> {
                    if (currentPart.isNotEmpty()) parent.invalidFormat("unexpected $ch")
                    positioninig = if (ch == '-') Positioning.LEFT else Positioning.CENTER
                }
                in "*#_=" -> {
                    if( !isStart ) parent.invalidFormat("bad fill char $ch position")
                    fillChar = ch
                }
                '0' -> {
                    if (currentPart.isEmpty()) fillChar = '0'
                    else currentPart.append(ch)
                }
                in "0123456789" -> {
                    currentPart.append(ch)
                }
                'd', 'i' -> {
                    createIntegerField()
                    break
                }
                else -> parent.invalidFormat("unexpected character '$ch'")
            }
            pos++
        }
    }

    private val isStart: Boolean get() = pos == 0

    private fun createIntegerField() {
        parseSize()
        insertField(parent.getNumber(index).toLong().toString())
    }

    private fun parseSize() {
        if (currentPart.isNotEmpty()) {
            size = currentPart.toString().toInt()
            currentPart.clear()
        }
    }

    private fun insertField(text: String) {
        if (size < 0 || size < text.length) {
            parent.specificationDone(text)
        } else {
            var padStart = 0
            var padEnd = 0
            when (positioninig) {
                Positioning.LEFT -> padEnd = size - text.length
                Positioning.RIGHT -> padStart = size - text.length
                Positioning.CENTER -> {
                    padStart = (size - text.length) / 2
                    padEnd = size - padStart - text.length

                }
            }
            val result = StringBuilder()
            while (padStart-- > 0) result.append(fillChar)
            result.append(text)
            while (padEnd-- > 0) result.append(fillChar)
            parent.specificationDone(result.toString())
        }
    }
}