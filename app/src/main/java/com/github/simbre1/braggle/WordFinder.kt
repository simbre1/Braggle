package com.github.simbre1.braggle

import java.util.*


class WordFinder(var board: Board, var dictionary: Dictionary) {

    fun find() : TreeSet<String> {
        val words = TreeSet<String>()

        for (row in 0 until board.size()) {
            for (col in 0 until board.size()) {
                val visited = Array(board.size()) { Array(board.size()) { false} }
                val s = StringBuilder()
                recursiveFind(words, visited, row, col, s)
            }
        }

        return words
    }

    private fun recursiveFind (words: TreeSet<String>,
                               visited: Array<Array<Boolean>>,
                               row: Int,
                               col: Int,
                               s: StringBuilder) {
        visited[row][col] = true
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

                if (!visited[i][j]) {
                    recursiveFind(words, visited, i, j, s)
                }
            }
        }

        s.deleteCharAt(s.length - 1)
        visited[row][col] = false
    }
}