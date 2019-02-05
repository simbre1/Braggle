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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.simbre1.braggle.data.DictionaryRepo
import com.github.simbre1.braggle.domain.Game
import com.github.simbre1.braggle.domain.Seed
import com.github.simbre1.braggle.viewmodel.GameModel
import com.github.simbre1.braggle.viewmodel.GameModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.*
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.InvitationCallback
import com.google.android.gms.games.multiplayer.Multiplayer
import com.google.android.gms.games.multiplayer.Participant
import com.google.android.gms.games.multiplayer.realtime.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.handleCoroutineException
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync
import java.lang.Exception
import kotlin.random.Random

class MainActivity : BaseActivity() {

    private val mediaPlayer = MediaPlayer().apply {
        setOnPreparedListener { start() }
        setOnCompletionListener { reset() }
    }

    private var alertDialog: AlertDialog? = null

    private lateinit var gameModel: GameModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)

        findViewById<Button>(R.id.button_single_player).setOnClickListener {
            switchToMainScreen()
        }

        findViewById<SignInButton>(R.id.button_sign_in).setOnClickListener {
            startSignInIntent()
        }

        findViewById<Button>(R.id.button_sign_out).setOnClickListener {
            signOut()
        }

        findViewById<Button>(R.id.button_invite_players).setOnClickListener {
            switchToScreen(R.id.screen_wait)

            // show list of invitable players
            mRealTimeMultiplayerClient
                ?.getSelectOpponentsIntent(1, 3)
                ?.addOnSuccessListener { startActivityForResult(intent, RC_SELECT_PLAYERS) }
                ?.addOnFailureListener { e -> handleException(e, "There was a problem selecting opponents.") }
        }

        findViewById<Button>(R.id.button_see_invitations).setOnClickListener {
            switchToScreen(R.id.screen_wait);

            // show list of pending invitations
            mInvitationsClient
                ?.getInvitationInboxIntent()
                ?.addOnSuccessListener { intent -> startActivityForResult(intent, RC_INVITATION_INBOX) }
                ?.addOnFailureListener { e -> handleException(e, "There was a problem getting the inbox.") }
        }

        findViewById<Button>(R.id.button_accept_popup_invitation).setOnClickListener {
            // user wants to accept the invitation shown on the invitation popup
            // (the one we got through the OnInvitationReceivedListener).
            mIncomingInvitationId?.let { id -> acceptInviteToRoom(id) }
            mIncomingInvitationId = null
        }

        val factory = GameModelFactory(DictionaryRepo(application))
        gameModel = ViewModelProviders.of(this, factory).get(GameModel::class.java)

        boardView.wordListeners.add { word ->
            val currentGame = gameModel.game.value
            if (currentGame != null) {
                onWord(currentGame, word)
            }
        }

        gameModel.game.observe(this, Observer { game ->
            supportActionBar?.title = getString(
                R.string.title_activity_main_seed,
                game.board.seed.seedString ?: ""
            )
            boardView.setBoard(game.board)
            updateFoundString(game)
            boardView.setActive(game.isRunning())
        })

        if (gameModel.game.value == null) {
            loadLastGame()
        }
    }

    override fun onResume() {
        super.onResume()

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently()
    }

    override fun onStop() {
        mInvitationsClient?.unregisterInvitationCallback(mInvitationCallback)
        leaveRoom()

        // stop trying to keep the screen on
        stopKeepingScreenOn()

        switchToMainScreen()

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
                qrView.addOnLayoutChangeListener { _: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->
                    doAsync {
                        val encode = QRCodeWriter().encode(
                            it.board.seed.toQr(),
                            BarcodeFormat.QR_CODE,
                            qrView.width,
                            qrView.width
                        )

                        val bitMatrixWidth = encode.width
                        val bitMatrixHeight = encode.height

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
        R.id.action_multiplayer -> {
            switchToScreen(R.id.screen_sign_in)
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
                data?.getStringExtra("SCAN_RESULT")?.also { marshalledSeed ->
                    Seed.fromQr(marshalledSeed)?.also { seed ->
                        createNewGame(seed)
                    }
                }
            }
        } else if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "Sign in")
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                task.getResult(ApiException::class.java)
                    ?.let { onConnected(it) }
            } catch (apiException: ApiException) {
                val message =
                    if (apiException.message.isNullOrEmpty()) getString(R.string.signin_other_error)
                    else apiException.message
                Log.d(TAG, apiException.message, apiException)

                onDisconnected()

                AlertDialog.Builder (this)
                    .setMessage(message)
                    .setNeutralButton(android.R.string.ok, null)
                    .show()
            }
        } else if (requestCode == RC_SELECT_PLAYERS) {
            // we got the result from the "select players" UI -- ready to create the room
            handleSelectPlayersResult(resultCode, intent)

        } else if (requestCode == RC_INVITATION_INBOX) {
            // we got the result from the "select invitation" UI (invitation inbox). We're
            // ready to accept the selected invitation:
            handleInvitationInboxResult(resultCode, intent)

        } else if (requestCode == RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).")
                TODO("startGame(true)")
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                leaveRoom()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                leaveRoom()
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
                    boardView.context.getColorFromAttr(R.attr.colorDiceCorrectWord)
                )
            } else {
                boardView.highlightTiles(
                    tiles,
                    boardView.context.getColorFromAttr(R.attr.colorDiceRepeatedWord)
                )
            }
            updateFoundString(game)
        } else {
            boardView.highlightTiles(
                tiles,
                boardView.context.getColorFromAttr(R.attr.colorDiceWrongWord)
            )
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
            words
        )
    }

    private fun vibrate(milliseconds: Long) {
        if (defaultSharedPreferences.getBoolean("vibrate", false)
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) {
            with(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator) {
                if (hasVibrator()) {
                    vibrate(
                        VibrationEffect.createOneShot(
                            milliseconds,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
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
                    it.declaredLength
                )
                prepareAsync()
            }
        }
    }

    private fun resetGameVars() {

    }

    var mCurScreen = -1


    // Client used to sign in with Google APIs
    private var mGoogleSignInClient: GoogleSignInClient? = null

    // Client used to interact with the real time multiplayer system.
    private var mRealTimeMultiplayerClient: RealTimeMultiplayerClient? = null

    // Client used to interact with the Invitation system.
    private var mInvitationsClient: InvitationsClient? = null

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    var mRoomId: String? = null

    // Holds the configuration of the current room.
    var mRoomConfig: RoomConfig? = null

    // Are we playing in multiplayer mode?
    private var mMultiplayer = false

    // The participants in the currently active game
    var mParticipants: ArrayList<Participant>? = null

    // My participant ID in the currently active game
    var mMyId: String? = null

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    var mIncomingInvitationId: String? = null

    // Message buffer for sending messages
    var mMsgBuf = ByteArray(2)

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    private val mOnRealTimeMessageReceivedListener = OnRealTimeMessageReceivedListener { realTimeMessage ->
        val buf = realTimeMessage.messageData
        val sender = realTimeMessage.senderParticipantId
        Log.d(TAG, "Message received from " + sender + ": " + buf.toString())
    }

    /**
     * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
     * your Activity's onActivityResult function
     */
    private fun startSignInIntent() {
        mGoogleSignInClient?.also {
            startActivityForResult(it.signInIntent, RC_SIGN_IN)
        }
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     *
     *
     * If the user has already signed in previously, it will not show dialog.
     */
    private fun signInSilently() {
        Log.d("multiplayer", "signInSilently()")

        mGoogleSignInClient
            ?.silentSignIn()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInSilently(): success")
                    onConnected(task.result!!)
                } else {
                    Log.d(TAG, "signInSilently(): failure", task.exception)
                    onDisconnected()
                }
            }
    }

    private fun signOut() {
        Log.d("multiplayer", "signOut()")

        mGoogleSignInClient
            ?.signOut()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signOut(): success")
                } else {
//                    handleException(task.getException(), "signOut() failed!");
                    Log.d(TAG, "signOut() failed!", task.exception)
                }
                onDisconnected()
            }
    }

    fun switchToScreen(screenId: Int) {
        // make the requested screen visible; hide all others.
        for (id in SCREENS) {
            findViewById<View>(id).visibility = if (screenId == id) View.VISIBLE else View.GONE
        }
        mCurScreen = screenId

        // should we show the invitation popup?
        val showInvPopup: Boolean
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false
        } else if (mMultiplayer) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = mCurScreen == R.id.game_view
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = mCurScreen == R.id.game_view
        }

        invitation_popup.visibility = if (showInvPopup) View.VISIBLE else View.GONE
    }

    private fun switchToMainScreen() {
//        if (mRealTimeMultiplayerClient != null) {
            switchToScreen(R.id.game_view)
//        } else {
//            switchToScreen(R.id.screen_sign_in)
//        }
    }

    // Leave the room.
    private fun leaveRoom() {
        Log.d(TAG, "Leaving room.")
//        mSecondsLeft = 0;
        stopKeepingScreenOn()

        if (mRoomId != null) {
            val roomId = mRoomId ?: return
            val roomConfig = mRoomConfig ?: return

            mRealTimeMultiplayerClient
                ?.leave(roomConfig, roomId)
                ?.addOnCompleteListener {
                    mRoomId = null
                    mRoomConfig = null
                }
            switchToScreen(R.id.screen_wait)
        } else {
            switchToMainScreen()
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    private fun showWaitingRoom(room: Room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        mRealTimeMultiplayerClient
            ?.getWaitingRoomIntent(room, MIN_PLAYERS)
            ?.addOnSuccessListener { startActivityForResult(intent, RC_WAITING_ROOM) }
            ?.addOnFailureListener { Log.d(TAG, "There was a problem getting the waiting room!") }
    }

    private val mInvitationCallback = object : InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        override fun onInvitationReceived(invitation: Invitation) {
            // We got an invitation to play a game! So, store it in
            // mIncomingInvitationId
            // and show the popup on the screen.
            mIncomingInvitationId = invitation.invitationId
            findViewById<TextView>(R.id.incoming_invitation_text).text = getString(
                R.string.is_inviting_you,
                invitation.inviter.displayName
            )
            switchToScreen(mCurScreen)
        }

        override fun onInvitationRemoved(invitationId: String) {
            if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
                mIncomingInvitationId = null
                switchToScreen(mCurScreen) // This will hide the invitation popup
            }
        }
    }

    var mPlayerId: String? = null

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    private var mSignedInAccount: GoogleSignInAccount? = null

    private fun onConnected(googleSignInAccount: GoogleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs '${googleSignInAccount.id}'")
        if (mSignedInAccount != googleSignInAccount) {
            mSignedInAccount = googleSignInAccount
            // update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount)
            mInvitationsClient = Games.getInvitationsClient(this, googleSignInAccount)

            // get the playerId from the PlayersClient
            val playersClient = Games.getPlayersClient(this, googleSignInAccount)
            playersClient.currentPlayer
                .addOnSuccessListener {
                    mPlayerId = it.playerId
                    switchToMainScreen()
                }
                .addOnFailureListener {
                    //                    createFailureListener("There was a problem getting the player id!")
                    Log.d(TAG, "There was a problem getting the player id!")
                }
        }

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        mInvitationsClient?.registerInvitationCallback(mInvitationCallback)

        // get the invitation from the connection hint
        // Retrieve the TurnBasedMatch from the connectionHint
        val gamesClient = Games.getGamesClient(this, googleSignInAccount)
        gamesClient.activationHint
            .addOnSuccessListener {
                if (it != null) {
                    val invitation =
                        it.getParcelable<Invitation>(Multiplayer.EXTRA_INVITATION)

                    if (invitation != null && invitation.invitationId != null) {
                        // retrieve and cache the invitation ID
                        Log.d(TAG, "onConnected: connection hint has a room invite!")
                        acceptInviteToRoom(invitation.invitationId)
                    }
                }
            }
            .addOnFailureListener {
                //                createFailureListener("There was a problem getting the activation hint!")
                Log.d(TAG, "There was a problem getting the activation hint!")
            }
    }

    private fun onDisconnected() {
        Log.d(TAG, "onDisconnected()")

        mRealTimeMultiplayerClient = null
        mInvitationsClient = null

        switchToMainScreen()
    }

    private val mRoomStatusUpdateCallback = object : RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        override fun onConnectedToRoom(room: Room?) {
            Log.d(TAG, "onConnectedToRoom.")

            //get participants and my ID:
            mParticipants = room?.participants
            mMyId = room?.getParticipantId(mPlayerId)

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room?.roomId
            }

            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: $mRoomId")
            Log.d(TAG, "My ID $mMyId")
            Log.d(TAG, "<< CONNECTED TO ROOM>>")
        }

        // Called when we get disconnected from the room. We return to the main screen.
        override fun onDisconnectedFromRoom(room: Room?) {
            mRoomId = null
            mRoomConfig = null
            showGameError()
        }


        // We treat most of the room update callbacks in the same way: we update our list of
        // participants and update the display. In a real game we would also have to check if that
        // change requires some action like removing the corresponding player avatar from the screen,
        // etc.
        override fun onPeerDeclined(
            room: Room?,
            arg1: List<String>
        ) {
            updateRoom(room)
        }

        override fun onPeerInvitedToRoom(
            room: Room?,
            arg1: List<String>
        ) {
            updateRoom(room)
        }

        override fun onP2PDisconnected(participant: String) {
        }

        override fun onP2PConnected(participant: String) {
        }

        override fun onPeerJoined(
            room: Room?,
            arg1: List<String>
        ) {
            updateRoom(room)
        }

        override fun onPeerLeft(
            room: Room?,
            peersWhoLeft: List<String>
        ) {
            updateRoom(room)
        }

        override fun onRoomAutoMatching(room: Room?) {
            updateRoom(room)
        }

        override fun onRoomConnecting(room: Room?) {
            updateRoom(room)
        }

        override fun onPeersConnected(
            room: Room?,
            peers: List<String>
        ) {
            updateRoom(room)
        }

        override fun onPeersDisconnected(
            room: Room?,
            peers: List<String>
        ) {
            updateRoom(room)
        }
    }

    // Show error message about game being cancelled and return to main screen.
    private fun showGameError() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.game_problem))
            .setNeutralButton(android.R.string.ok, null).create()
        switchToMainScreen()
    }

    private val mRoomUpdateCallback = object : RoomUpdateCallback() {

        // Called when room has been created
        override fun onRoomCreated(
            statusCode: Int,
            room: Room?
        ) {
            if (room == null) {
                return
            }

            Log.d(TAG, "onRoomCreated($statusCode, $room)")
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status $statusCode")
                showGameError()
                return
            }

            // save room ID so we can leave cleanly before the game starts.
            mRoomId = room.roomId

            // show the waiting room UI
            showWaitingRoom(room)
        }

        // Called when room is fully connected.
        override fun onRoomConnected(
            statusCode: Int,
            room: Room?
        ) {
            if (room == null) {
                return
            }

            Log.d(TAG, "onRoomConnected($statusCode, $room)")
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status $statusCode")
                showGameError()
                return
            }
            updateRoom(room)
        }

        override fun onJoinedRoom(
            statusCode: Int,
            room: Room?
        ) {
            if (room == null) {
                return
            }

            Log.d(TAG, "onJoinedRoom($statusCode, $room)")
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status $statusCode")
                showGameError()
                return
            }

            // show the waiting room UI
            showWaitingRoom(room)
        }

        // Called when we've successfully left the room (this happens a result of voluntarily leaving
        // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
        override fun onLeftRoom(
            statusCode: Int,
            roomId: String
        ) {
            // we have left the room; return to main screen.
            Log.d(TAG, "onLeftRoom, code $statusCode")
            switchToMainScreen()
        }
    }

    private fun updateRoom(room: Room?) {
        if (room != null) {
            mParticipants = room.participants
        }
        if (mParticipants != null) {
//            updatePeerScoresDisplay()
        }
    }


    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private fun handleSelectPlayersResult(response: Int, data: Intent) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, $response")
            switchToMainScreen()
            return
        }

        Log.d(TAG, "Select players UI succeeded.")

        // get the invitee list
        val invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS)
        Log.d(TAG, "Invitee count: " + invitees.size)

        // get the automatch criteria
        var autoMatchCriteria: Bundle? = null
        val minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0)
        val maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0)
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0)
            Log.d(TAG, "Automatch criteria: $autoMatchCriteria")
        }

        // create the room
        Log.d(TAG, "Creating room...")
        switchToScreen(R.id.screen_wait)
        keepScreenOn()
        resetGameVars()

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
            .addPlayersToInvite(invitees)
            .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
            .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
            .setAutoMatchCriteria(autoMatchCriteria)
            .build()
            .also {
                mRealTimeMultiplayerClient?.create(it)
            }
        Log.d(TAG, "Room created, waiting for it to be ready...")
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private fun handleInvitationInboxResult(response: Int, data: Intent) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, $response")
            switchToMainScreen()
            return
        }

        Log.d(TAG, "Invitation inbox UI succeeded.")
        val invitation = data.extras?.getParcelable<Invitation>(Multiplayer.EXTRA_INVITATION)

        // accept invitation
        if (invitation != null) {
            acceptInviteToRoom(invitation.invitationId)
        }
    }

    // Accept the given invitation.
    private fun acceptInviteToRoom(invitationId: String) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: $invitationId")

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
            .setInvitationIdToAccept(invitationId)
            .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
            .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
            .build()
            .also {
                mRealTimeMultiplayerClient
                    ?.join(it)
                    ?.addOnSuccessListener {
                        Log.d(TAG, "Room Joined Successfully!")
                    }
            }

        switchToScreen(R.id.screen_wait)
        keepScreenOn()
        resetGameVars()
    }

    /*
     * MISC SECTION. Miscellaneous methods.
     */


    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // Clears the flag that keeps the screen on.
    private fun stopKeepingScreenOn() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun handleException(exception: Exception, message: String) {
        Log.d(TAG, message, exception)
    }

    companion object {
        const val ALL_WORDS = "com.github.simbre1.braggle.ALL_WORDS"
        const val DICTIONARY_LOOKUP_INTENT_PACKAGE = "com.github.simbre1.braggle.DICTIONARY_LOOKUP_INTENT_PACKAGE"
        const val DICTIONARY_LOOKUP_URL = "com.github.simbre1.braggle.DICTIONARY_LOOKUP_URL"
        const val RC_QR_SCANNER = 1

        const val RC_SIGN_IN = 9001
        const val RC_WAITING_ROOM = 9002
        const val RC_SELECT_PLAYERS = 9003
        const val RC_INVITATION_INBOX = 9004

        const val TAG = "multiplayer"
        const val MIN_PLAYERS = 1
        const val MAX_PLAYERS = 8

        // This array lists all the individual screens our game has.
        val SCREENS = intArrayOf(
            R.id.game_view,
            R.id.screen_sign_in,
            R.id.screen_wait
        )
    }
}
