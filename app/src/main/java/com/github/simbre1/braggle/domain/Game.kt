package com.github.simbre1.braggle.domain

import com.github.simbre1.braggle.data.DictionaryRepo
import com.github.simbre1.braggle.data.GameData
import java.util.*

class Game(val uid: Int?,
           val board: Board,
           val language: Language,
           val minWordLength: Int,
           val allWords: TreeSet<String>,
           val foundWords: TreeSet<String>,
           private val startTime: Date,
           private var stopTime: Date?) {

    fun isWord(word: String) = allWords.contains(word)

    fun addWord(word: String) = if(isRunning()) { foundWords.add(word) } else { false }

    fun isRunning() = stopTime == null

    fun stop() {
        stopTime = Date()
    }

    fun getStopTime() = stopTime

    fun getScore() = getScore(foundWords)

    fun getMaxScore() = getScore(allWords)

    fun toGameData(): GameData {
        return GameData(
            uid,
            language.code,
            board.size(),
            minWordLength,
            board.seed.seed,
            board.seed.seedString,
            board.getLetters(),
            allWords,
            foundWords,
            startTime,
            stopTime)
    }

    fun toQr(): String {
        val size = board.size()
        val lang = language.code
        val seed = board.seed.marshal()
        return "braggle:$lang:$size:$minWordLength:$seed"
    }

    companion object {
        fun create(gameData: GameData): Game {
            return Game(
                gameData.uid,
                Board.create(
                    Seed(gameData.seed, gameData.seedString),
                    gameData.board),
                Language.fromCode(gameData.language)!!,
                gameData.minWordLength,
                gameData.allWords,
                gameData.foundWords,
                gameData.startTime,
                gameData.stopTime)
        }

        fun fromQr(str: String?,
                   dictRepo: DictionaryRepo): Game? {
            if (str.isNullOrBlank()) {
                return null
            }

            val list = str.split(":")
            if (list.size < 5) {
                return null
            }

            val language = Language.fromCode(list[1]) ?: return null
            val size = list[2].toInt()
            val minWordLength = list[3].toInt()
            val seed = Seed.unmarshal(list[4]) ?: return null

            val board = Board.create(language, size, seed)
            val dictionary = dictRepo.get(language)

            return Game(
                null,
                board,
                language,
                minWordLength,
                WordFinder(board, dictionary).find(minWordLength),
                TreeSet(),
                Date(),
                null)
        }

        fun getScore(words: Collection<String>) : Int {
            return words.map {
                when (it.length) {
                    0, 1, 2 -> 0
                    3, 4 -> 1
                    5 -> 2
                    6 -> 3
                    7 -> 5
                    else -> 11
                }
            }.sum()
        }
    }
}