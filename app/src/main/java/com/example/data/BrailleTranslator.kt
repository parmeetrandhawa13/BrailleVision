package com.example.data

object BrailleTranslator {

    private val charToBrailleMap = mapOf(
        'a' to '⠁', 'b' to '⠃', 'c' to '⠉', 'd' to '⠙', 'e' to '⠑',
        'f' to '⠋', 'g' to '⠛', 'h' to '⠓', 'i' to '⠊', 'j' to '⠚',
        'k' to '⠅', 'l' to '⠇', 'm' to '⠍', 'n' to '⠝', 'o' to '⠕',
        'p' to '⠏', 'q' to '⠟', 'r' to '⠗', 's' to '⠎', 't' to '⠞',
        'u' to '⠥', 'v' to '⠧', 'w' to '⠺', 'x' to '⠭', 'y' to '⠽', 'z' to '⠵',
        '1' to '⠁', '2' to '⠃', '3' to '⠉', '4' to '⠙', '5' to '⠑',
        '6' to '⠋', '7' to '⠛', '8' to '⠓', '9' to '⠊', '0' to '⠚',
        ',' to '⠂', ';' to '⠆', ':' to '⠒', '.' to '⠲', '!' to '⠖',
        '?' to '⠦', '\'' to '⠄', '-' to '⠤', ' ' to ' '
    )

    private val brailleToCharMap = charToBrailleMap.entries
        .filter { it.key !in '1'..'9' && it.key != '0' }
        .associate { it.value to it.key }

    private val brailleContractions = mapOf(
        '⠮' to "the",
        '⠯' to "and",
        '⠾' to "for",
        '⠷' to "of",
        '⠿' to "with",
        '⠱' to "wh",
        '⠹' to "th",
        '⠡' to "ch",
        '⠩' to "sh",
        '⠪' to "ow",
        '⠳' to "ou",
        '⠬' to "ing",
        '⠻' to "er",
        '⠜' to "ar",
        '⠫' to "ed",
        '⠢' to "en",
        '⠔' to "in",
        '⠣' to "gh"
    )

    const val BRAILLE_NUMBER_PREFIX = '⠼'
    const val BRAILLE_CAPITAL_PREFIX = '⠠'

    fun textToBraille(text: String): String {
        val result = StringBuilder()
        var inNumber = false

        for (char in text) {
            when {
                char.isUpperCase() -> {
                    inNumber = false
                    result.append(BRAILLE_CAPITAL_PREFIX)
                    result.append(charToBrailleMap[char.lowercaseChar()] ?: char)
                }
                char.isDigit() -> {
                    if (!inNumber) {
                        result.append(BRAILLE_NUMBER_PREFIX)
                        inNumber = true
                    }
                    result.append(charToBrailleMap[char] ?: char)
                }
                char.isWhitespace() -> {
                    inNumber = false
                    result.append(char)
                }
                else -> {
                    inNumber = false
                    result.append(charToBrailleMap[char] ?: char)
                }
            }
        }
        return result.toString()
    }

    fun brailleToText(braille: String): String {
        val result = StringBuilder()
        var isCapital = false
        var isNumber = false

        var i = 0
        while (i < braille.length) {
            val c = braille[i]
            when (c) {
                BRAILLE_CAPITAL_PREFIX -> {
                    isCapital = true
                }
                BRAILLE_NUMBER_PREFIX -> {
                    isNumber = true
                }
                ' ' -> {
                    isNumber = false
                    isCapital = false
                    result.append(' ')
                }
                '\n' -> {
                    isNumber = false
                    isCapital = false
                    result.append('\n')
                }
                else -> {
                    if (isNumber) {
                        val digitChar = when (c) {
                            '⠁' -> '1'; '⠃' -> '2'; '⠉' -> '3'; '⠙' -> '4'; '⠑' -> '5'
                            '⠋' -> '6'; '⠛' -> '7'; '⠓' -> '8'; '⠊' -> '9'; '⠚' -> '0'
                            else -> brailleToCharMap[c] ?: c
                        }
                        result.append(digitChar)
                    } else if (brailleContractions.containsKey(c)) {
                        val contraction = brailleContractions[c].orEmpty()
                        if (isCapital) {
                            result.append(contraction.replaceFirstChar { it.uppercase() })
                            isCapital = false
                        } else {
                            result.append(contraction)
                        }
                    } else {
                        val regularChar = brailleToCharMap[c] ?: c
                        if (isCapital) {
                            result.append(regularChar.uppercaseChar())
                            isCapital = false
                        } else {
                            result.append(regularChar)
                        }
                    }
                }
            }
            i++
        }
        return result.toString()
    }
}
