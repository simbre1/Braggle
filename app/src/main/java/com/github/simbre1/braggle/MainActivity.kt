package com.github.simbre1.braggle

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import java.util.function.Consumer

const val ALL_WORDS = "com.github.simbre1.braggle.ALL_WORDS"

class MainActivity : AppCompatActivity() {

    private val minWordLength = 3
    private var defaultDict : Dictionary? = null
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

        newGameButton.setOnClickListener { view ->
            newGame()
        }

        defaultDict = Dictionary(
                applicationContext.assets.open("eowl-v1.1.2.txt")
                        .bufferedReader()
                        .readLines()
                        .filter { s -> s.length >= minWordLength }
                        .map { s -> s.toUpperCase() }
                        .toCollection(TreeSet()))

        boardView.wordListeners.add(Consumer<String>{ word -> onWord(word) })

        newGame()
    }

    private fun showAllWords() {
        val currentGame = game ?: return
        val list = currentGame.allWords.map { Pair(it, currentGame.foundWords.contains(it)) }

        val intent = Intent(this, AllWordsActivity::class.java).apply {
            putExtra(ALL_WORDS, list.toTypedArray())
        }
        startActivity(intent)
    }

    private fun onWord(word: String) {
        val currentGame = game ?: return

        if (currentGame.isWord(word)) {
            currentGame.addWord(word)
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
        val currentDict = defaultDict ?: return

        val board = Board.random(4)
        boardView.setBoard(board)

        wordView.text = getString(R.string.loading_new_game)

        doAsync {
            val newGame = Game(board, WordFinder(board, currentDict).find())
            uiThread {
                game = newGame
                updateFoundString(newGame)
            }
        }
    }
}
