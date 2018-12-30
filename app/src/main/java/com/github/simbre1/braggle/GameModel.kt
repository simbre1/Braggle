package com.github.simbre1.braggle

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import org.jetbrains.anko.doAsync

class GameModel(private val dictionaryRepo: DictionaryRepo) : ViewModel() {

    val game: MutableLiveData<Game> = MutableLiveData()
    val minWordLength = 4

    fun createNewGameAsync(dictionary: String,
                           minWordLength: Int,
                           boardSize: Int) {
        doAsync {
            createNewGame(dictionary, minWordLength, boardSize)
        }
    }

    fun createNewGame(dictionary: String,
                      minWordLength: Int,
                      boardSize: Int) {
        val startTime = System.nanoTime()

        val language = Language.fromCode(dictionary) ?: Language.EN

        val board = Board.random(language, boardSize)
        val dict = dictionaryRepo.get(language)
        val newGame = Game(
            board,
            dict,
            WordFinder(board, dict).find(minWordLength))

        Log.d("createNewGame", "dict:" + dictionary + " minWordLength:" + minWordLength + " boardSize:" + boardSize)
        Log.d("createNewGame", "time:" + (System.nanoTime() - startTime))

        game.postValue(newGame)
    }
}