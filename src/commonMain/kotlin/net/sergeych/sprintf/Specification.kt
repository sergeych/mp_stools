package net.sergeych.sprintf

import net.sergeych.sprintf.net.sergeych.mp_logger.Sprintf

internal enum class Positioning {
    LEFT, RIGHT, CENTER
}

internal class Specification(val parent: Sprintf, var index: Int) {

    enum class Stage {
        FLAGS,
        LENGTH,
        FRACTION
    }

    private var stage = Stage.FLAGS

    private var size: Int = -1
    private var fractionalPartSize: Int = -1
    private var positioninig = Positioning.RIGHT
    private var fillChar = ' '
    private var currentPart = StringBuilder()
    private var pos = 0
    private var explicitPlus = false
    private var done = false

    private val isScanningFlags: Boolean
        get() = stage == Stage.FLAGS

    internal fun scan() {
        while (!done) {
            val ch = parent.nextChar()
//            println("spec: $ch: $stage [$currentPart]")
            when (ch) {
                '-', '^' -> {
                    if (!isScanningFlags) parent.invalidFormat("unexpected $ch")
                    positioninig = if (ch == '-') Positioning.LEFT else Positioning.CENTER
                }
                '+' -> {
                    if (!isScanningFlags) parent.invalidFormat("unexpected $ch")
                    explicitPlus = true
                }
                in "*#_=" -> {
                    if (!isScanningFlags) parent.invalidFormat("bad fill char $ch position")
                    fillChar = ch
                }
                '0' -> {
                    if (isScanningFlags) fillChar = '0'
                    else
                        currentPart.append(ch)
                }
                in "123456789" -> {
                    if( stage == Stage.FLAGS ) stage = Stage.LENGTH
                    currentPart.append(ch)
                }
                's' -> createStringField()
                'd', 'i' -> createIntegerField()
                'o' -> createOctalField()
                'x' -> createHexField(false)
                'X' -> createHexField(true)
                'f', 'F' -> createFloat()
                'E' -> createScientific(true)
                'e' -> createScientific(false)
                'g' -> createAutoFloat(true)
                'G' -> createAutoFloat(false)
                'c', 'C' -> createCharacter()
                '.' -> {
                    when(stage) {
                        Stage.FLAGS -> stage = Stage.FRACTION
                        Stage.LENGTH -> {
                            endStage(false)
                            stage = Stage.FRACTION
                        }
                        else -> parent.invalidFormat("can't parse specification: unexpected '.'")
                    }
                }
                else -> parent.invalidFormat("unexpected character '$ch'")
            }
            pos++
        }
    }

    private fun createStringField() {
        endStage()
        insertField(parent.getText(index))
    }

    private fun createIntegerField() {
        endStage()
        val number = parent.getNumber(index).toLong()
        if (explicitPlus && fillChar == '0' && number > 0)
            insertField(number.toString(), "+")
        else
            insertField(if (explicitPlus) "+$number" else "$number")
    }

    private fun createHexField(upperCase: Boolean) {
        endStage()
        val number = parent.getNumber(index).toLong()
        if (explicitPlus) parent.invalidFormat("'+' is incompatible with hex format")
        val text = number.toString(16)
        insertField(if (upperCase) text.uppercase() else text.lowercase())
    }

    private fun createOctalField() {
        endStage()
        val number = parent.getNumber(index).toLong()
        if (explicitPlus) parent.invalidFormat("'+' is incompatible with oct format")
        insertField(number.toString(8))
    }

    private fun createCharacter() {
        endStage()
        insertField(parent.getCharacter(index).toString())
    }

    private fun endStage(setDone: Boolean = true) {
        if( setDone ) done = true
        if (currentPart.isNotEmpty()) {
            when(stage) {
                Stage.LENGTH -> size = currentPart.toString().toInt()
                Stage.FRACTION -> fractionalPartSize = currentPart.toString().toInt()
                Stage.FLAGS -> parent.invalidFormat("can't parse format specifier (error 7)")
            }
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

    private fun createFloat() {
        endStage()
        val number = parent.getNumber(index).toDouble()
        val t = fractionalFormat(number, size, fractionalPartSize)

        if (explicitPlus && fillChar == '0' && number > 0)
            insertField(t, "+")
        else
            insertField(if (explicitPlus) "+$t" else t)
    }

    private fun createScientific(upperCase: Boolean) {
        endStage()
        val number = parent.getNumber(index).toDouble()
        val t = scientificFormat(number, size, fractionalPartSize).let {
            if( upperCase ) it.uppercase() else it.lowercase()
        }

        if (explicitPlus && fillChar == '0' && number > 0)
            insertField(t, "+")
        else
            insertField(if (explicitPlus) "+$t" else t)
    }

    private fun createAutoFloat(upperCase: Boolean) {
        endStage()
        val number = parent.getNumber(index)
        val t = number.toString().let {
            if( upperCase ) it.uppercase() else it.lowercase()
        }

        if (explicitPlus && fillChar == '0' && number.toDouble() > 0)
            insertField(t, "+")
        else
            insertField(if (explicitPlus) "+$t" else t)
    }
}