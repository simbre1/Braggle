package com.github.simbre1.braggle

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import org.jetbrains.anko.doAsync
import kotlin.random.Random

class GameModel(private val dictionaryRepo: DictionaryRepo) : ViewModel() {

    val game: MutableLiveData<Game> = MutableLiveData()

    fun createNewGameAsync(dictionary: String,
                           minWordLength: Int,
                           boardSize: Int,
                           seed: String?) {
        doAsync {
            createNewGame(dictionary, minWordLength, boardSize, seed)
        }
    }

    fun createNewGame(dictionary: String,
                      minWordLength: Int,
                      boardSize: Int,
                      seed: String?) {
        val startTime = System.nanoTime()

        val language = Language.fromCode(dictionary) ?: Language.EN

        val seedLong = stringToSeed(seed)
        val board = Board.random(language, boardSize, seedLong)
        val dict = dictionaryRepo.get(language)
        val newGame = Game(
            board,
            dict,
            WordFinder(board, dict).find(minWordLength),
            true)

        Log.d(
            "createNewGame",
            "dict:" + dictionary +
                    " minWordLength:" + minWordLength +
                    " boardSize:" + boardSize +
                    " seed:" + seed + "=" + seedLong)
        Log.d("createNewGame", "time:" + (System.nanoTime() - startTime))

        game.postValue(newGame)
    }

    companion object {
        private fun stringToSeed(s: String?): Long {
            val trimmed = s?.trim() ?: ""
            return if (trimmed.isEmpty()) {
                Random.nextLong()
            } else {
                trimmed.hashCode().toLong()
            }
        }
    }
}