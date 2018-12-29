package com.github.simbre1.braggle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import java.util.function.Consumer

const val ALL_WORDS = "com.github.simbre1.braggle.ALL_WORDS"
const val DICTIONARY_LOOKUP_INTENT_PACKAGE = "com.github.simbre1.braggle.DICTIONARY_LOOKUP_INTENT_PACKAGE"

class MainActivity : AppCompatActivity() {

    private val minWordLength = 3
    private var defaultDictEnglish : Dictionary? = null
    private var game: Game? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Show all words", Snackbar.LENGTH_LONG)
                    .setAction("Next") { showAllWords() }
                    .show()
        }

        newGameButton.setOnClickListener { newGame() }

        defaultDictEnglish = Dictionary(
                applicationContext.assets.open("eowl-v1.1.2.txt")
                        .bufferedReader()
                        .readLines()
                        .filter { s -> s.length >= minWordLength }
                        .map { s -> s.toUpperCase() }
                        .toCollection(TreeSet()),
            "livio.pack.lang.en_US")

        boardView.wordListeners.add(Consumer { word -> onWord(word) })

        newGame()
    }

    private fun showAllWords() {
        val currentGame = game ?: return
        val list = currentGame.allWords.map { Pair(it, currentGame.foundWords.contains(it)) }

        val intent = Intent(this, AllWordsActivity::class.java).apply {
            putExtra(ALL_WORDS, list.toTypedArray())
            putExtra(DICTIONARY_LOOKUP_INTENT_PACKAGE, currentGame.dictionary.lookupIntentPackage)
        }
        startActivity(intent)
    }

    private fun onWord(word: String) {
        val currentGame = game ?: return

        if (currentGame.isWord(word)) {
            if (currentGame.addWord(word)) {
                val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
                if (vibratorService != null && vibratorService.hasVibrator()) {
                    vibratorService.vibrate(
                        VibrationEffect.createOneShot(
                            200,
                            VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
            updateFoundString(currentGame)
        }
    }

    private fun updateFoundString(game: Game) {
        val words = game.foundWords.toString()
        val foundN = game.foundWords.size
        val allN = game.allWords.size
        wordView.text = getString(R.string.found_words, foundN, allN, words)
    }

    private fun newGame() {
        val currentDict = defaultDictEnglish ?: return

        val board = Board.random(4)
        boardView.setBoard(board)

        wordView.text = getString(R.string.loading_new_game)

        doAsync {
            val newGame = createNewGame(board, currentDict)
            uiThread {
                game = newGame
                updateFoundString(newGame)
            }
        }
    }

    companion object {
        private fun createNewGame(board: Board, dict: Dictionary) : Game {
            val startTime = System.nanoTime()

            val game = Game(board, dict, WordFinder(board, dict).find())

            Log.d("createNewGame", "time:" + (System.nanoTime() - startTime))

            return game
        }
    }
}
