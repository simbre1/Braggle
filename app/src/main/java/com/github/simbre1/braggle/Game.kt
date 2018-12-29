package com.github.simbre1.braggle

import java.util.*

class Game(val board: Board,
           val dictionary: Dictionary,
           val allWords: TreeSet<String>) {

    val foundWords = TreeSet<String>()

    fun isWord(word: String) = allWords.contains(word)

    fun addWord(word: String) = foundWords.add(word)
}