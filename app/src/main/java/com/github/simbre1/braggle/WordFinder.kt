package com.github.simbre1.braggle

import java.util.*


class WordFinder(var board: Board, var dictionary: Dictionary) {

    fun find() : TreeSet<String> = find(0)

    fun find(minWordLength: Int) : TreeSet<String> {
        val words = TreeSet<String>()

        for (row in 0 until board.size()) {
            for (col in 0 until board.size()) {
                recursiveFind(
                    words,
                    dictionary.getWords(),
                    0,
                    board.size(),
                    row,
                    col,
                    mutableListOf(),
                    minWordLength)
            }
        }

        return words
    }

    private fun recursiveFind (foundWords: TreeSet<String>,
                               dictWords: SortedSet<String>,
                               visited: Long,
                               size: Int,
                               row: Int,
                               col: Int,
                               dice: MutableList<String>,
                               minWordLength: Int) : Long {
        val currentBit = 1L shl (row * size) + col
        var hasVisited = visited or currentBit
        dice.add(board.at(row, col).toUpperCase())

        val word = dice.joinToString("")
        val tailset = dictWords.tailSet(word)

        if (!tailset.isEmpty()) {
            val iter = tailset.iterator()
            val tailWord = iter.next()
            if (tailWord == word && tailWord.length >= minWordLength){
                foundWords.add(word)
            }

            if ((tailWord != word && tailWord.startsWith(word))
                || (tailWord == word && iter.hasNext() && iter.next().startsWith(word))) {
                for (i in row - 1..row + 1) {
                    if (i < 0 || i >= board.size()) {
                        continue
                    }

                    for (j in col - 1..col + 1) {
                        if (j < 0 || j >= board.size()) {
                            continue
                        }

                        val bit = 1L shl (i * size) + j
                        if (hasVisited and bit == 0L) {
                            hasVisited = recursiveFind(
                                foundWords,
                                tailset,
                                hasVisited,
                                size,
                                i,
                                j,
                                dice,
                                minWordLength)
                        }
                    }
                }
            }
        }

        dice.removeAt(dice.size - 1)

        return hasVisited and currentBit.inv()
    }
}