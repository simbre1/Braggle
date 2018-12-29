package com.github.simbre1.braggle

import java.util.*

class Board(private var letters: Array<Array<Char>>) {

    fun at(row: Int, col: Int) = letters[row][col]

    fun at(index: Pair<Int, Int>) = letters[index.first][index.second]

    fun size() = letters.size

    companion object Factory {

        fun random(size: Int) : Board {
            return random(
                size,
                Random().nextLong(),
                english16)
        }

        fun random(size: Int,
                   seed: Long,
                   dice: Array<String>): Board {
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

//        private val french16 = arrayOf(
//            "ETUKNO",
//            "EVGTIN",
//            "DECAMP",
//            "IELRUW",
//            "EHIFSE",
//            "RECALS",
//            "ENTDOS",
//            "OFXRIA",
//            "NAVEDZ",
//            "EIOATA",
//            "GLENYU",
//            "BMAQJO",
//            "TLIBRA",
//            "SPULTE",
//            "AIMSOR",
//            "ENHRIS")
//
//        private val english25 = arrayOf(
//            "AAAFRS",
//            "AAEEEE",
//            "AAFIRS",
//            "ADENNN",
//            "AEEEEM",
//            "AEEGMU",
//            "AEGMNN",
//            "AFIRSY",
//            "BBJKXZ",
//            "CCENST",
//            "EIILST",
//            "CEIPST",
//            "DDHNOT",
//            "DHHLOR",
//            "DHHNOW",
//            "DHLNOR",
//            "EIIITT",
//            "EILPST",
//            "EMOTTT",
//            "ENSSSU",
//            "FIPRSY",
//            "GORRVW",
//            "IPRSYY",
//            "NOOTUW",
//            "OOOTTU")
    }
}