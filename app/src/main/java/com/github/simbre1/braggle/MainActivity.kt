package com.github.simbre1.braggle

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.defaultSharedPreferences

const val ALL_WORDS = "com.github.simbre1.braggle.ALL_WORDS"
const val DICTIONARY_LOOKUP_INTENT_PACKAGE = "com.github.simbre1.braggle.DICTIONARY_LOOKUP_INTENT_PACKAGE"

class MainActivity : AppCompatActivity() {

    lateinit var gameModel: GameModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val factory = GameModelFactory(DictionaryRepo(application))
        gameModel = ViewModelProviders.of(this, factory).get(GameModel::class.java)

        boardView.wordListeners.add { word ->
            val currentGame = gameModel.game.value
            if(currentGame != null) {
                onWord(currentGame, word)
            }
        }

        gameModel.game.observe(this, android.arch.lifecycle.Observer { game ->
            if (game != null) {
                boardView.setBoard(game.board)
                updateFoundString(game)
            }
        })

        if (gameModel.game.value == null) {
            createNewGame()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.action_new_game -> {
            createNewGame()
            true
        }
        R.id.action_show_all_words -> {
            showAllWords()
            true
        }
        R.id.action_settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun createNewGame() {
        wordView.text = getString(R.string.loading_new_game)

        val language = defaultSharedPreferences.getString("language_preference", "en")
            ?: "en"
        val minWordLength = defaultSharedPreferences.getString("minimum_word_length_preference", "4")?.toInt()
            ?: 4
        val boardSize = defaultSharedPreferences.getString("board_size_preference", "4")?.toInt()
            ?: 4

        gameModel.createNewGameAsync(language, minWordLength, boardSize)
    }

    private fun showAllWords() {
        val game = gameModel.game.value ?: return

        val list = game.allWords.map { Pair(it, game.foundWords.contains(it)) }

        val intent = Intent(this, AllWordsActivity::class.java).apply {
            putExtra(ALL_WORDS, list.toTypedArray())
            putExtra(DICTIONARY_LOOKUP_INTENT_PACKAGE, game.dictionary.language.dictionaryIntentPackage)
        }
        startActivity(intent)
    }

    private fun onWord(game: Game, word: String) {
        if (game.isWord(word)) {
            if (game.addWord(word)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
                    if (vibratorService != null && vibratorService.hasVibrator()) {
                        vibratorService.vibrate(
                            VibrationEffect.createOneShot(
                                200,
                                VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            }
            updateFoundString(game)
        }
    }

    private fun updateFoundString(game: Game) {
        val words = game.foundWords.toString()
        val foundN = game.foundWords.size
        val allN = game.allWords.size
        wordView.text = getString(
            R.string.found_words,
            foundN,
            allN,
            game.dictionary.language.displayName,
            words)
    }
}
