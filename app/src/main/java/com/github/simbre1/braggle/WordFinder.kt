package com.github.simbre1.braggle

import java.util.*


class WordFinder(var board: Board, var dictionary: Dictionary) {

    fun find() : TreeSet<String> {
        val words = TreeSet<String>()

        for (row in 0 until board.size()) {
            for (col in 0 until board.size()) {
                recursiveFind(words, 0, board.size(), row, col, StringBuilder())
            }
        }

        return words
    }

    private fun recursiveFind (words: TreeSet<String>,
                               visited: Long,
                               size: Int,
                               row: Int,
                               col: Int,
                               s: StringBuilder) : Long {
        val currentBit = 1L shl (row * size) + col
        var hasVisited = visited or currentBit
        s.append(board.at(row, col))

        val word = s.toString()
        if (dictionary.isWord(word))
            words.add(word)

        for (i in row - 1..row + 1) {
            if (i < 0 || i >= board.size()) {
                continue
            }

            for (j in col - 1..col + 1) {
                if (j < 0 || j >= board.size()) {
                    continue
                }

                val bit = 1L shl (i * size) + j;
                if (hasVisited and bit == 0L) {
                    hasVisited = recursiveFind(words, hasVisited, size, i, j, s)
                }
            }
        }

        s.deleteCharAt(s.length - 1)

        return hasVisited and currentBit.inv()
    }
}