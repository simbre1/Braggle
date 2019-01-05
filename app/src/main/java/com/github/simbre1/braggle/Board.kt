package com.github.simbre1.braggle

import java.util.*

class Board(private var letters: Array<Array<Char>>) {

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
            val dice = getDice(language, size)
            if (dice.isEmpty()) {
                throw IllegalArgumentException("no dice")
            }

            val rand = Random(seed)
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

        private fun getDice(language: Language, boardSize: Int): Array<String> =
            when (language) {
                Language.EN -> if (boardSize < 5) english16 else english25
                Language.FR -> french16
                Language.NL -> TODO()
            }

        private val english16 = arrayOf(
            "AAEEGN",
            "ABBJOO",
            "ACHOPS",
            "AFFKPS",
            "AOOTTW",
            "CIMOTU",
            "DEILRX",
            "DELRVY",
            "DISTTY",
            "EEGHNW",
            "EEINSU",
            "EHRTVW",
            "EIOSST",
            "ELRTTY",
            "HIMNUQ",
            "HLNNRZ")

        private val french16 = arrayOf(
            "ETUKNO",
            "EVGTIN",
            "DECAMP",
            "IELRUW",
            "EHIFSE",
            "RECALS",
            "ENTDOS",
            "OFXRIA",
            "NAVEDZ",
            "EIOATA",
            "GLENYU",
            "BMAQJO",
            "TLIBRA",
            "SPULTE",
            "AIMSOR",
            "ENHRIS")

        private val english25 = arrayOf(
            "AAAFRS",
            "AAEEEE",
            "AAFIRS",
            "ADENNN",
            "AEEEEM",
            "AEEGMU",
            "AEGMNN",
            "AFIRSY",
            "BBJKXZ",
            "CCENST",
            "EIILST",
            "CEIPST",
            "DDHNOT",
            "DHHLOR",
            "DHHNOW",
            "DHLNOR",
            "EIIITT",
            "EILPST",
            "EMOTTT",
            "ENSSSU",
            "FIPRSY",
            "GORRVW",
            "IPRSYY",
            "NOOTUW",
            "OOOTTU")
    }
}