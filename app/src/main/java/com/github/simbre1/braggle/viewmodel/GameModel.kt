package com.github.simbre1.braggle.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.simbre1.braggle.data.AppDatabase
import com.github.simbre1.braggle.data.DictionaryRepo
import com.github.simbre1.braggle.domain.*
import org.jetbrains.anko.doAsync
import java.util.*

class GameModel(private val dictionaryRepo: DictionaryRepo) : ViewModel() {

    val game: MutableLiveData<Game> = MutableLiveData()

    fun createNewGameAsync(dictionary: String,
                           minWordLength: Int,
                           boardSize: Int,
                           seed: Seed) {
        doAsync {
            createNewGame(dictionary, minWordLength, boardSize, seed)
        }
    }

    fun createNewGame(dictionary: String,
                      minWordLength: Int,
                      boardSize: Int,
                      seed: Seed) {
        val startTime = System.nanoTime()

        val language = Language.fromCode(dictionary)
            ?: Language.EN

        val board = Board.create(language, boardSize, seed)
        val dict = dictionaryRepo.get(language)
        val newGame = Game(
            null,
            board,
            language,
            minWordLength,
            WordFinder(board, dict).find(minWordLength),
            TreeSet(),
            Date(),
            null)

        Log.d(
            "createNewGame",
            "dict:$dictionary minWordLength:$minWordLength boardSize:$boardSize seed:$seed")
        Log.d("createNewGame", "time:" + (System.nanoTime() - startTime))

        game.postValue(newGame)
    }

    fun createNewGameFromQr(str: String) {
        doAsync {
            Game.fromQr(str, dictionaryRepo)?.also {
                game.postValue(it)
            }
        }
    }

    fun loadLastGameAsync(context: Context,
                          onFail: () -> Unit) {
        doAsync {
            AppDatabase.getInstance(context)
                .gameDataDao()
                .getLast()?.also {
                    game.postValue(Game.create(it))
                } ?: onFail.invoke()
        }
    }

    fun save(context: Context) {
        doAsync {
            game.value?.apply {
                if (uid == null) {
                    AppDatabase.getInstance(context)
                        .gameDataDao()
                        .insertAll(toGameData())
                    loadLastGameAsync(context) {}
                } else {
                    AppDatabase.getInstance(context)
                        .gameDataDao()
                        .update(uid, foundWords, getStopTime())
                }
            }
        }
    }
}