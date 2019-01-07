package com.github.simbre1.braggle.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.simbre1.braggle.data.DictionaryRepo
import com.github.simbre1.braggle.domain.Board
import com.github.simbre1.braggle.domain.Game
import com.github.simbre1.braggle.domain.Language
import com.github.simbre1.braggle.domain.WordFinder
import org.jetbrains.anko.doAsync
import java.util.*

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

        val language = Language.fromCode(dictionary)
            ?: Language.EN

        val board = Board.random(language, boardSize, seed)
        val dict = dictionaryRepo.get(language)
        val newGame = Game(
            null,
            board,
            dict,
            WordFinder(board, dict).find(minWordLength),
            Date(),
            null)

        Log.d(
            "createNewGame",
            "dict:$dictionary minWordLength:$minWordLength boardSize:$boardSize seed:$seed")
        Log.d("createNewGame", "time:" + (System.nanoTime() - startTime))

        game.postValue(newGame)
    }
}