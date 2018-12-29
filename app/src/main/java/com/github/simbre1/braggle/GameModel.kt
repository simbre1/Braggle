package com.github.simbre1.braggle

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import org.jetbrains.anko.doAsync

class GameModel(private val dictionaryRepo: DictionaryRepo) : ViewModel() {

    val game: MutableLiveData<Game> = MutableLiveData()
    val minWordLength = 4

    fun createNewGameAsync() {
        doAsync {
            createNewGame()
        }
    }

    fun createNewGame() {
        val startTime = System.nanoTime()

        val board = Board.random(4)
        val dictionary = dictionaryRepo.getEnglish()
        val newGame = Game(
            board,
            dictionary,
            WordFinder(board, dictionary).find())

        Log.d("createNewGame", "time:" + (System.nanoTime() - startTime))

        game.postValue(newGame)
    }
}