package com.github.simbre1.braggle

import java.util.*

class Board(private var letters: Array<Array<String>>) {

    fun at(row: Int, col: Int) = letters[row][col]

    fun at(index: Pair<Int, Int>) = letters[index.first][index.second]

    fun size() = letters.size

    fun getLetters() = letters.joinToString { it -> it.contentToString() }

    companion object Factory {

        fun random(langauge: Language, size: Int) : Board {
            return random(
                langauge,
                size,
                Random().nextLong())
        }

        fun random(language: Language,
                   size: Int,
                   seed: String?)
                = random(language, size, stringToSeed(seed))

        fun random(language: Language,
                   size: Int,
                   seed: Long): Board {
            val rand = Random(seed)
            val dice = mutableListOf<Array<String>>()
            dice.addAll(getDice(language, size))
            if (dice.isEmpty()) {
                throw IllegalArgumentException("no dice")
            }
            dice.shuffle(rand)

            val letters = Array(size) { i ->
                Array(size) { j ->
                    val diceNr = ((i*size) + j) % dice.size
                    dice[diceNr][rand.nextInt(6)]
                }
            }

            return Board(letters)
        }

        private fun stringToSeed(s: String?): Long {
            val trimmed = s?.trim() ?: ""
            return if (trimmed.isEmpty()) {
                kotlin.random.Random.nextLong()
            } else {
                trimmed.hashCode().toLong()
            }
        }

        private fun getDice(language: Language, boardSize: Int): Array<Array<String>> =
            when (language) {
                Language.EN -> if (boardSize < 5) english16 else english25
                Language.FR, Language.NL -> french16
            }

        private val english16 = arrayOf(
            arrayOf("A","A","E","E","G","N"),
            arrayOf("A","B","B","J","O","O"),
            arrayOf("A","C","H","O","P","S"),
            arrayOf("A","F","F","K","P","S"),
            arrayOf("A","O","O","T","T","W"),
            arrayOf("C","I","M","O","T","U"),
            arrayOf("D","E","I","L","R","X"),
            arrayOf("D","E","L","R","V","Y"),
            arrayOf("D","I","S","T","T","Y"),
            arrayOf("E","E","G","H","N","W"),
            arrayOf("E","E","I","N","S","U"),
            arrayOf("E","H","R","T","V","W"),
            arrayOf("E","I","O","S","S","T"),
            arrayOf("E","L","R","T","T","Y"),
            arrayOf("H","I","M","N","U","Qu"),
            arrayOf("H","L","N","N","R","Z"))

        private val french16 = arrayOf(
            arrayOf("E","T","U","K","N","O"),
            arrayOf("E","V","G","T","I","N"),
            arrayOf("D","E","C","A","M","P"),
            arrayOf("I","E","L","R","U","W"),
            arrayOf("E","H","I","F","S","E"),
            arrayOf("R","E","C","A","L","S"),
            arrayOf("E","N","T","D","O","S"),
            arrayOf("O","F","X","R","I","A"),
            arrayOf("N","A","V","E","D","Z"),
            arrayOf("E","I","O","A","T","A"),
            arrayOf("G","L","E","N","Y","U"),
            arrayOf("B","M","A","Qu","J","O"),
            arrayOf("T","L","I","B","R","A"),
            arrayOf("S","P","U","L","T","E"),
            arrayOf("A","I","M","S","O","R"),
            arrayOf("E","N","H","R","I","S"))

        private val english25 = arrayOf(
            arrayOf("A","A","A","F","R","S"),
            arrayOf("A","A","E","E","E","E"),
            arrayOf("A","A","F","I","R","S"),
            arrayOf("A","D","E","N","N","N"),
            arrayOf("A","E","E","E","E","M"),
            arrayOf("A","E","E","G","M","U"),
            arrayOf("A","E","G","M","N","N"),
            arrayOf("A","F","I","R","S","Y"),
            arrayOf("B","B","J","K","X","Z"),
            arrayOf("C","C","E","N","S","T"),
            arrayOf("E","I","I","L","S","T"),
            arrayOf("C","E","I","P","S","T"),
            arrayOf("D","D","H","N","O","T"),
            arrayOf("D","H","H","L","O","R"),
            arrayOf("D","H","H","N","O","W"),
            arrayOf("D","H","L","N","O","R"),
            arrayOf("E","I","I","I","T","T"),
            arrayOf("E","I","L","P","S","T"),
            arrayOf("E","M","O","T","T","T"),
            arrayOf("E","N","S","S","S","U"),
            arrayOf("F","I","P","R","S","Y"),
            arrayOf("G","O","R","R","V","W"),
            arrayOf("I","P","R","S","Y","Y"),
            arrayOf("N","O","O","T","U","W"),
            arrayOf("O","O","O","T","T","U"))
    }
}