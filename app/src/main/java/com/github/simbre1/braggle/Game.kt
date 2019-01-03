package com.github.simbre1.braggle

import java.util.*

class Game(val board: Board,
           val dictionary: Dictionary,
           val allWords: TreeSet<String>,
           private var running: Boolean) {

    val foundWords = TreeSet<String>()

    fun isWord(word: String) = allWords.contains(word)

    fun addWord(word: String) = if(isRunning()) { foundWords.add(word) } else { false }

    fun isRunning() = running

    fun stop() {
        running = false
    }
}