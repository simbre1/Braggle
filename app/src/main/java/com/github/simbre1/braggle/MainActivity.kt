package com.github.simbre1.braggle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.simbre1.braggle.data.DictionaryRepo
import com.github.simbre1.braggle.domain.Game
import com.github.simbre1.braggle.domain.Seed
import com.github.simbre1.braggle.viewmodel.GameModel
import com.github.simbre1.braggle.viewmodel.GameModelFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync
import kotlin.random.Random

const val ALL_WORDS = "com.github.simbre1.braggle.ALL_WORDS"
const val DICTIONARY_LOOKUP_INTENT_PACKAGE = "com.github.simbre1.braggle.DICTIONARY_LOOKUP_INTENT_PACKAGE"
const val DICTIONARY_LOOKUP_URL = "com.github.simbre1.braggle.DICTIONARY_LOOKUP_URL"
const val RC_QR_SCANNER = 1

class MainActivity : BaseActivity() {

    private val mediaPlayer = MediaPlayer().apply {
        setOnPreparedListener { start() }
        setOnCompletionListener { reset() }
    }

    private var alertDialog: AlertDialog? = null

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

        gameModel.game.observe(this, Observer { game ->
            supportActionBar?.title = getString(
                R.string.title_activity_main_seed,
                game.board.seed.seedString ?: "")
            boardView.setBoard(game.board)
            updateFoundString(game)
            boardView.setActive(game.isRunning())
        })

        if (gameModel.game.value == null) {
            loadLastGame()
        }
    }

    override fun onStop() {
        alertDialog?.dismiss()
        gameModel.save(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.action_new_game -> {
            val builder = AlertDialog.Builder(this)

            val view = View.inflate(this, R.layout.new_game, null)
            val seedView = view.findViewById<EditText>(R.id.seedText)
            val qrButton = view.findViewById<ImageButton>(R.id.qrButton)
            val qrView = view.findViewById<ImageView>(R.id.qrView)

            qrButton.setOnClickListener {
                val intent = Intent("com.google.zxing.client.android.SCAN")
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
                startActivityForResult(intent, RC_QR_SCANNER)
            }

            gameModel.game.value?.also {
                qrView.addOnLayoutChangeListener { view: View, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int ->
                    doAsync {
                        val encode = QRCodeWriter().encode(
                            it.toQr(),
                            BarcodeFormat.QR_CODE,
                            qrView.width,
                            qrView.width
                        )

                        val bitMatrixWidth = encode.getWidth()
                        val bitMatrixHeight = encode.getHeight()

                        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

                        for (y in 0 until bitMatrixHeight) {
                            val offset = y * bitMatrixWidth
                            for (x in 0 until bitMatrixWidth) {
                                pixels[offset + x] = if (encode.get(x, y)) Color.BLACK else Color.WHITE
                            }
                        }
                        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)
                        bitmap.setPixels(pixels, 0, qrView.width, 0, 0, bitMatrixWidth, bitMatrixHeight)
                        qrView.post {
                            qrView.setImageBitmap(bitmap)
                        }
                    }
                }
            }

            builder.apply {
                setPositiveButton(R.string.ok) { _, _ -> createNewGame(Seed.create(seedView.text.toString())) }
                setNegativeButton(R.string.cancel) { _, _ -> }
            }
            builder.setTitle(R.string.confirm_new_game)
            builder.setView(view)
            alertDialog = builder.create()
            alertDialog?.show()

            true
        }
        R.id.action_show_all_words -> {
            gameModel.game.value?.run {
                if (isRunning()) {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.apply {
                        setPositiveButton(R.string.ok) { _, _ -> showAllWords() }
                        setNegativeButton(R.string.cancel) { _, _ -> }
                    }
                    builder.setTitle(R.string.confirm_end_game)
                    builder.create().show()
                } else {
                    showAllWords()
                }
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_QR_SCANNER) {
            if (resultCode == Activity.RESULT_OK) {
                data?.getStringExtra("SCAN_RESULT")?.also {
                    val seed = Seed.fromQr(it)
                    if (seed != null) {
                        createNewGame(seed)
                    } else {
                        gameModel.createNewGameFromQr(it)
                    }
                }
            }
        }
    }

    private fun loadLastGame() {
        wordView.text = getString(R.string.loading_new_game)
        gameModel.loadLastGameAsync(this) { createNewGame(Seed.create()) }
    }

    private fun createNewGame(seed: Seed) {
        wordView.text = getString(R.string.loading_new_game)

        val language = defaultSharedPreferences.getString("language_preference", "en")
            ?: "en"
        val minWordLength = defaultSharedPreferences.getString("minimum_word_length_preference", "4")?.toInt()
            ?: 4
        val boardSize = defaultSharedPreferences.getString("board_size_preference", "4")?.toInt()
            ?: 4

        gameModel.createNewGameAsync(language, minWordLength, boardSize, seed)
    }

    private fun showAllWords() {
        gameModel.game.value?.run {
            stop()
            boardView.setActive(false)

            val list = allWords.map { Pair(it, foundWords.contains(it)) }
            val intent = Intent(this@MainActivity, AllWordsActivity::class.java).apply {
                putExtra(ALL_WORDS, list.toTypedArray())
                putExtra(DICTIONARY_LOOKUP_INTENT_PACKAGE, language.dictionaryIntentPackage)
                putExtra(DICTIONARY_LOOKUP_URL, language.dictionaryUrl)
            }
            startActivity(intent)
        }
    }

    private fun onWord(game: Game, tiles: List<Tile>) {
        val word = tiles
            .joinToString("") { it.str }
            .toUpperCase()

        val cow = resources
            ?.getStringArray(R.array.happyCow)
            ?.contains(word)
            ?: false

        if (cow) {
            boardView.showAnimatedCow()
        }

        if (game.isWord(word)) {
            if (game.addWord(word)) {
                vibrate(200)
                playCowbell()
                boardView.highlightTiles(
                    tiles,
                    boardView.context.getColorFromAttr(R.attr.colorDiceCorrectWord))
                gameModel.save(this)
            } else {
                boardView.highlightTiles(
                    tiles,
                    boardView.context.getColorFromAttr(R.attr.colorDiceRepeatedWord))
            }
            updateFoundString(game)
        } else {
            boardView.highlightTiles(
                tiles,
                boardView.context.getColorFromAttr(R.attr.colorDiceWrongWord))
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
            game.language.displayName,
            game.getScore(),
            game.getMaxScore(),
            words)
    }

    private fun vibrate(milliseconds: Long) {
        if (defaultSharedPreferences.getBoolean("vibrate", false)
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator) {
                if (hasVibrator()) {
                    vibrate(
                        VibrationEffect.createOneShot(
                            milliseconds,
                            VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        }
    }

    private fun playCowbell() {
        playSound(
            resources.getIdentifier(
                "cowbell_" + Random.nextInt(1, 23),
                "raw",
                packageName
            )
        )
    }

    private fun playSound(@RawRes rawResId: Int) {
        if (!defaultSharedPreferences.getBoolean("sound_effects", false)) {
            return
        }

        applicationContext.resources.openRawResourceFd(rawResId)?.let {
            mediaPlayer.run {
                reset()
                setDataSource(
                    it.fileDescriptor,
                    it.startOffset,
                    it.declaredLength)
                prepareAsync()
            }
        }
    }
}
