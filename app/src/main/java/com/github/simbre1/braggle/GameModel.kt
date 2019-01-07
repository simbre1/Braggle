package com.github.simbre1.braggle

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.jetbrains.anko.doAsync

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

        val board = Board.random(language, boardSize, seed)
        val dict = dictionaryRepo.get(language)
        val newGame = Game(
            board,
            dict,
            WordFinder(board, dict).find(minWordLength),
            true)

        Log.d(
            "createNewGame",
            "dict:$dictionary minWordLength:$minWordLength boardSize:$boardSize seed:$seed")
        Log.d("createNewGame", "time:" + (System.nanoTime() - startTime))

        game.postValue(newGame)
    }
}