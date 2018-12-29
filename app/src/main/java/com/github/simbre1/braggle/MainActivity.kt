package com.github.simbre1.braggle

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.function.Consumer

const val ALL_WORDS = "com.github.simbre1.braggle.ALL_WORDS"
const val DICTIONARY_LOOKUP_INTENT_PACKAGE = "com.github.simbre1.braggle.DICTIONARY_LOOKUP_INTENT_PACKAGE"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val factory = GameModelFactory(DictionaryRepo(application))
        val gameModel = ViewModelProviders.of(this, factory).get(GameModel::class.java)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Show all words", Snackbar.LENGTH_LONG)
                    .setAction("Next") {
                        val currentGame = gameModel.game.value
                        if(currentGame != null) {
                            showAllWords(currentGame)
                        }
                    }
                    .show()
        }

        newGameButton.setOnClickListener { gameModel.createNewGame() }
        boardView.wordListeners.add(Consumer { word ->
            val currentGame = gameModel.game.value
            if(currentGame != null) {
                onWord(currentGame, word)
            }
        })

        gameModel.game.observe(this, android.arch.lifecycle.Observer { game ->
            if (game != null) {
                boardView.setBoard(game.board)
                updateFoundString(game)
            }
        })

        if (gameModel.game.value == null) {
            gameModel.createNewGame()
        }
    }

    private fun showAllWords(game: Game) {
        val list = game.allWords.map { Pair(it, game.foundWords.contains(it)) }

        val intent = Intent(this, AllWordsActivity::class.java).apply {
            putExtra(ALL_WORDS, list.toTypedArray())
            putExtra(DICTIONARY_LOOKUP_INTENT_PACKAGE, game.dictionary.lookupIntentPackage)
        }
        startActivity(intent)
    }

    private fun onWord(game: Game, word: String) {
        if (game.isWord(word)) {
            if (game.addWord(word)) {
                val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
                if (vibratorService != null && vibratorService.hasVibrator()) {
                    vibratorService.vibrate(
                        VibrationEffect.createOneShot(
                            200,
                            VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
            updateFoundString(game)
        }
    }

    private fun updateFoundString(game: Game) {
        val words = game.foundWords.toString()
        val foundN = game.foundWords.size
        val allN = game.allWords.size
        wordView.text = getString(R.string.found_words, foundN, allN, words)
    }
}
