package com.github.simbre1.braggle

import java.util.*

class Board(private var letters: Array<Array<Char>>) {

    fun at(row: Int, col: Int) = letters[row][col]

    fun at(index: Pair<Int, Int>) = letters[index.first][index.second]

    fun size() = letters.size

    companion object Factory {

        private val defaultCharset = arrayListOf(
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')

        fun random(size: Int,
                   seed: Long) : Board {
            val rand = Random(seed)
            var letters = Array(size) { Array(size) { defaultCharset[rand.nextInt(defaultCharset.size)] } }
            return Board(letters)
        }

        fun random(size: Int) : Board {
            return random(size, Random().nextLong())
        }
    }
}