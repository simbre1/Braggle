package com.github.simbre1.braggle

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import java.util.function.Consumer

const val ALL_WORDS = "com.github.simbre1.braggle.ALL_WORDS"
const val FOUND_WORDS = "com.github.simbre1.braggle.FOUND_WORDS"

class MainActivity : AppCompatActivity() {

    private val minWordLength = 3
    private var defaultDict : Dictionary? = null
    private var game: Game? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action") { showAllWords() }
                    .show()
        }

        defaultDict = Dictionary(
                applicationContext.assets.open("eowl-v1.1.2.txt")
                        .bufferedReader()
                        .readLines()
                        .filter { s -> s != null && s.length >= minWordLength }
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

            val words = currentGame.foundWords.toString()
            val foundN = currentGame.foundWords.size
            val allN = currentGame.allWords.size
            wordView.text =  "Found $foundN/$allN: $words"
        }
    }

    private fun newGame() {
        val currentDict = defaultDict ?: return

        val board = Board.random(4)
        val allWords = WordFinder(board, currentDict).find()
        game = Game(board, allWords)

        boardView.setBoard(board)
    }
}
