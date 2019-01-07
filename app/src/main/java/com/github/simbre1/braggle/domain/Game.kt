package com.github.simbre1.braggle.domain

import com.github.simbre1.braggle.data.GameData
import java.util.*

class Game(val uid: Int?,
           val board: Board,
           val dictionary: Dictionary,
           val allWords: TreeSet<String>,
           val startTime: Date,
           private var stopTime: Date?) {

    val foundWords = TreeSet<String>()

    fun isWord(word: String) = allWords.contains(word)

    fun addWord(word: String) = if(isRunning()) { foundWords.add(word) } else { false }

    fun isRunning() = stopTime == null

    fun stop() {
        stopTime = Date()
    }

    fun toGameData(): GameData {
        return GameData(
            uid,
            dictionary.language.code,
            board.size(),
            board.seed,
            board.seedString,
            board.getLetters(),
            foundWords.joinToString { ";" },
            startTime,
            stopTime)
    }

    companion object {
        fun create(gameData: GameData) {
            TODO()
        }
    }
}