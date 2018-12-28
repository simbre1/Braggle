package com.github.simbre1.braggle

import java.util.*

class Game(private val board: Board,
           val allWords: TreeSet<String>) {

    val foundWords = TreeSet<String>()

    fun isWord(word: String) = allWords.contains(word)

    fun isFound(word: String) = foundWords.contains(word)

    fun addWord(word: String) = foundWords.add(word)

    fun getCompletionRatio() = if (allWords.isEmpty()) 1 else foundWords.size / allWords.size
}