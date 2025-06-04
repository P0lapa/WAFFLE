// MainActivityGame.kt
package com.example.waffle_front

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waffle_front.OthersActivity.ActionCardsAdapter
import com.example.waffle_front.OthersActivity.ContentCard
import com.example.waffle_front.OthersActivity.GameRepository
import com.example.waffle_front.OthersActivity.PlayerData
import com.example.waffle_front.OthersActivity.PlayersAdapter
import com.example.waffle_front.OthersActivity.ServerManager
import com.example.waffle_front.OthersActivity.SituationCard
import com.example.waffle_front.OthersActivity.TableAdapter
import com.example.waffle_front.OthersActivity.WebSocketManager

class MainActivityGame : AppCompatActivity() {
    // Добавляем адаптер
    private lateinit var myLogin: String
    private lateinit var myId: String
    private lateinit var roomId: String
    private var cardsPerPlayer = 6
    private lateinit var playersAdapter: PlayersAdapter
    private lateinit var actionsAdapter: ActionCardsAdapter
    private lateinit var serverManager: ServerManager
    private lateinit var tableAdapter: TableAdapter


    private var firstPlayerLoad = true
    private var lastOthersCount = 0
    private lateinit var roleTextView: TextView
    private lateinit var moodTextView: TextView
    private var lastRole: String? = null
    private var lastMood: String? = null
    private var droppedCounter = 0
    private var isCreator: Boolean = false

    @SuppressLint("WrongViewCast", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        when (intent.getStringExtra("force_orientation")) {
            "landscape" -> requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
//            "portrait"  -> requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_game)
        serverManager = ServerManager(this)
        roomId = intent.getStringExtra("room_code") ?: return
        isCreator = intent.getBooleanExtra("is_creator", false)

        val roomCodeView: TextView = findViewById(R.id.roomCodeTextView)
        roomCodeView.text = roomId

        val startBtn: Button = findViewById(R.id.startGameButton)
//        startBtn.isEnabled = false

        WebSocketManager.connect(roomId) {
            startBtn.isEnabled = true
        }

        if (isCreator) {
            findViewById<View>(R.id.showRoomCodeLayout).visibility = View.VISIBLE
            getCreatorLogin()
            startBtn.setOnClickListener {
                if (!WebSocketManager.isConnected()) {
                    Toast.makeText(this, "Соединение НЕ установлено!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                WebSocketManager.sendStartMessage(
                    onSuccess = {},
                    onError = { e -> Toast.makeText(this, e, Toast.LENGTH_SHORT).show() }
                )


            }
            val stopGameFrame: FrameLayout = findViewById(R.id.closeFrame)
            stopGameFrame.visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.showRoomCodeLayout).visibility = View.VISIBLE
            findViewById<View>(R.id.showRoomCodeWidget).visibility = View.GONE
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            myLogin = prefs.getString("my-login", "") ?: ""
            myId =prefs.getString("my-id", "") ?: ""
            observePlayersAfterLogin()
            getCardsPerPlayer()
        }

        setupPlayersRecycler() // инициализация RecyclerView и адаптер
        setupActionsRecycler() // инициализация RecyclerView и адаптер
        setupTableRecycler() // инициализация RecyclerView и адаптер

        GameRepository.droppedCards.observe(this) { dropped ->
            tableAdapter.submitList(dropped)
            val deckFrame2: FrameLayout = findViewById(R.id.deckFrame2)
            val deckFrame3: FrameLayout = findViewById(R.id.deckFrame3)
            val deckFrame4: FrameLayout = findViewById(R.id.deckFrame4)
            if(droppedCounter>3){
                deckFrame2.visibility = View.VISIBLE
                if(droppedCounter>4){
                    deckFrame3.visibility = View.VISIBLE
                    if(droppedCounter>5){
                        deckFrame4.visibility = View.VISIBLE
                    }
                }
            }
            droppedCounter++

        }
        // Пример кнопки “Выйти из игры”
//        findViewById<Button>(R.id.closeButton).setOnClickListener {
//            // Сначала разорвём WS
//            WebSocketManager.disconnect()
//            // Затем отправим HTTP DELETE
//            serverManager.leaveRoom(roomId, myId, object : ServerManager.LeaveRoomCallback {
//                override fun onSuccess() {
//                    Toast.makeText(this@MainActivityGame, "Вы вышли из игры", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//                override fun onError(errorMessage: String) {
//                    Toast.makeText(this@MainActivityGame, errorMessage, Toast.LENGTH_SHORT).show()
//                }
//            })
//        }

        GameRepository.gameStarted.observe(this) { event ->
            Toast.makeText(this, "Игра началась!", Toast.LENGTH_SHORT).show()
            val allPlayers = event.body.players
            val filtered = allPlayers.filter { it.login != myLogin }
            playersAdapter.submitList(filtered)

            val me = allPlayers.firstOrNull { it.login == myLogin }
            val myCards = me?.actionCards ?: emptyList()
            actionsAdapter.submitList(myCards)

            showSituationCard(event.body.situationCard)
            val dimBackground: View = findViewById(R.id.dimBackground)
            val showRoomCodeLayout: ConstraintLayout = findViewById(R.id.showRoomCodeLayout)
            dimBackground.visibility = View.GONE
            showRoomCodeLayout.visibility = View.GONE
        }

        val dimBackgroundMain: View = findViewById(R.id.dimBackgroundMain)
        val situationCardModal: FrameLayout = findViewById(R.id.situationCardModal) //модальное окно для карты ситуации
        val roleAndMoodCardModal: ConstraintLayout = findViewById(R.id.roleAndMoodCardModal) //модальное окно для карт роли и настроения
        val situationCardButton: ImageButton = findViewById(R.id.situationCardButton)
        val roleAndMoodCardButton: ImageButton = findViewById(R.id.roleAndMoodCardButton)
        val changesituationButton: ImageButton = findViewById(R.id.changeSituationButton)
        val changeRoleButton: ImageButton = findViewById(R.id.changeRoleButton)
        val changeMoodButton: ImageButton = findViewById(R.id.changeMoodButton)
        roleTextView = findViewById(R.id.roleTextModal)
        moodTextView = findViewById(R.id.moodTextModal)


        dimBackgroundMain.setOnClickListener {
            dimBackgroundMain.visibility = View.GONE
            situationCardModal.visibility = View.GONE
            changesituationButton.visibility = View.GONE
            roleAndMoodCardModal.visibility = View.GONE
        }

        situationCardButton.setOnClickListener {
            dimBackgroundMain.visibility = View.VISIBLE
            situationCardModal.visibility = View.VISIBLE
            if (isCreator) changesituationButton.visibility = View.VISIBLE
        }

        roleAndMoodCardButton.setOnClickListener {
            dimBackgroundMain.visibility = View.VISIBLE
            roleAndMoodCardModal.visibility = View.VISIBLE
//            changesituationButton.visibility = View.VISIBLE
        }

        changesituationButton.setOnClickListener { view ->
            onRotateButtonClick(view)

            val context = this@MainActivityGame // или view.context
            val serverManager = ServerManager(context)

            serverManager.changeSituationCard(
                roomId,
                object : ServerManager.ChangeSituationCallback {
                    override fun onSuccess(successMessage: String) {
                        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(errorMessage: String) {
//                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                })
        }

        changeRoleButton.setOnClickListener { view ->
            onRotateButtonClick(view)
            changeRoleCard()
        }

        changeMoodButton.setOnClickListener { view ->
            onRotateButtonClick(view)
            changeMoodCard()
        }

        setupSituationCardObserver()
    }

    private fun setupSituationCardObserver() {
        GameRepository.currentSituationCard.observe(this) { newCard ->
            updateSituationCardUI(newCard)

            Toast.makeText(
                this,
                "Карточка ситуации обновлена!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateSituationCardUI(card: SituationCard) {
        // Обновляем текст в модальном окне (если оно открыто)
//        findViewById<TextView>(R.id.situationText).text = card.content
        val textView = findViewById<TextView>(R.id.situationText)
        textView.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                textView.text = card.content
                textView.animate().alpha(1f).setDuration(200).start()
            }
            .start()

        val textViewModal = findViewById<TextView>(R.id.situationTextModal)
        textViewModal.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                textViewModal.text = card.content
                textViewModal.animate().alpha(1f).setDuration(200).start()
            }
            .start()

        Log.d("GameUI", "Updated situation card: ${card.content}")
    }

    override fun onDestroy() {
        WebSocketManager.disconnect()
        super.onDestroy()
    }

    private fun showSituationCard(card: SituationCard) {
        Log.d(
            "GAME_DEBUG", """
    Situation Card:
    ID: ${card.id}
    Content: "${card.content}"
    ===================================
    """.trimIndent()
        )

        val situationCardModal: TextView = findViewById(R.id.situationTextModal)
        situationCardModal.text = card.content
    }

    private fun updatePlayersUI(players: List<PlayerData>) {
        Log.d("PLAYERS_UPDATE", "Получено ${players.size} игроков")
        players.forEachIndexed { index, player ->
            Log.d(
                "PLAYER_${index + 1}", """
                ID: ${player.playerId}
                Логин: ${player.login}
                Роль: ${player.roleCard.content}
                Настроение: ${player.moodCard.content}
                Действий: ${player.actionCards.size}
            """.trimIndent()
            )
            if (index == players.size) {
                Toast.makeText(this, "Игрок ${player.login} присоеденился", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    fun onRotateButtonClick(view: View) {
        if (view.isClickable) {
            view.isClickable = false
            val animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
            animator.duration = 1000
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.doOnEnd { view.isClickable = true }
            animator.start()
        }
    }

    private fun setupPlayersRecycler() {
        playersAdapter = PlayersAdapter()
        val playersRecyclerView: RecyclerView = findViewById(R.id.playersRecyclerView)
        playersRecyclerView.layoutManager = LinearLayoutManager(
            this@MainActivityGame,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        // здесь было: "val adapter = playersAdapter" — это не привязывает адаптер к RV!
        playersRecyclerView.adapter = playersAdapter
        playersRecyclerView.addItemDecoration(SpacingItemDecoration(16))
    }

    private fun getCreatorLogin() {
        val serverManager = ServerManager(this)
        serverManager.getCreatorLogin(this, roomId, object : ServerManager.HttpCallback {
            override fun onSuccess(successMessage: String) {
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                myLogin = prefs.getString("creator-login", "") ?: ""
                myId = prefs.getString("creator-id", "") ?: ""
                Toast.makeText(this@MainActivityGame, "вы создатель: $myLogin", Toast.LENGTH_SHORT).show()
                // теперь, когда myLogin известен, подписываемся на список:
                observePlayersAfterLogin()
            }
            override fun onError(errorMessage: String) {
                Toast.makeText(this@MainActivityGame, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("ОШИБКА", "Текст ошибки: $errorMessage")
            }
        })
    }

    private fun getCardsPerPlayer() {
        val serverManager = ServerManager(this)
        serverManager.getCreatorLogin(this, roomId, object : ServerManager.HttpCallback {
            override fun onSuccess(successMessage: String) {
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                cardsPerPlayer  = prefs.getInt("cards-per-player", 6)
                Toast.makeText(this@MainActivityGame, "вы создатель: $myLogin", Toast.LENGTH_SHORT).show()
                // теперь, когда myLogin известен, подписываемся на список:
                observePlayersAfterLogin()
            }
            override fun onError(errorMessage: String) {
                Toast.makeText(this@MainActivityGame, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("ОШИБКА", "Текст ошибки: $errorMessage")
            }
        })
    }

    private fun observePlayersAfterLogin() {
        GameRepository.players.observe(this) { players ->
            val others = players.filter { it.login != myLogin }
            playersAdapter.submitList(others)

            val me = players.firstOrNull { it.login == myLogin }
            val myCards = me?.actionCards ?: emptyList()
            actionsAdapter.submitList(myCards)

            // роль:
            if (lastRole == null) {
                // при первом заходе просто устанавливаем текст без анимации
                if (me != null) {
                    roleTextView.text = me.roleCard.content
                }
            } else if (me != null) {
                if (me.roleCard.content != lastRole) {
                    // если роль изменилась — показываем анимацию
                    animateTextChange(roleTextView, me.roleCard.content)
                }
            }
            if (me != null) {
                lastRole = me.roleCard.content
            }

            // настроение:
            if (lastMood == null) {
                if (me != null) {
                    moodTextView.text = me.moodCard.content
                }
            } else if (me != null) {
                if (me.moodCard.content != lastMood) {
                    animateTextChange(moodTextView, me.moodCard.content)
                }
            }
            if (me != null) {
                lastMood = me.moodCard.content
            }

            if (!firstPlayerLoad) {
                if (others.size > lastOthersCount) {
                    val newPlayer = others.last()
                    Toast.makeText(
                        this,
                        "Игрок ${newPlayer.login} присоединился!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val currentActions = me?.actionCards
            if (currentActions != null) {
                actionsAdapter.submitList(currentActions)
            }

            val desiredCount = cardsPerPlayer
            if (currentActions != null) {
                if (currentActions.size < desiredCount && !isDeckOver()) {
                    drawNewCardForMe()
                }
                else if (isDeckOver()){
                    val deckFrame: FrameLayout = findViewById(R.id.deckFrame)
                    deckFrame.visibility = View.GONE
                }
            }
        }
    }

    private fun setupActionsRecycler() {
        actionsAdapter = ActionCardsAdapter { card ->
            // при нажатии сбрасываем карту через сервер
            dropMyActionCard(card)
        }
        val rvActions: RecyclerView = findViewById(R.id.actionsRecyclerView)
        rvActions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvActions.adapter = actionsAdapter
        rvActions.addItemDecoration(SpacingItemDecoration(16))
    }

    private fun dropMyActionCard(card: ContentCard) {
        // найдём playerId (у нас у PlayerData, вместо login может быть id – подставьте правильное поле)
        serverManager.dropActionCard(roomId, myId, card.id, object : ServerManager.DropActionCallback {
            override fun onSuccess() {
                // можно сразу локально удалить (не обязательно ждать WS):
                GameRepository.discardActionCard(myLogin, card.id)
                Toast.makeText(this@MainActivityGame, "Карта сброшена!", Toast.LENGTH_SHORT).show()
            }
            override fun onError(errorMessage: String) {
//                Toast.makeText(this@MainActivityGame, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun drawNewCardForMe() {
        serverManager.drawActionCard(roomId, myId, object : ServerManager.DrawActionCallback {
            override fun onSuccess(newPlayerData: PlayerData) {
                // UI обновится автоматически через GameRepository.replacePlayerData,
                // но можно показать тост:
                Toast.makeText(
                    this@MainActivityGame,
                    "Вам выдана новая карта!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(errorMessage: String) {
                Toast.makeText(
                    this@MainActivityGame,
                    errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun isDeckOver(): Boolean {
        return GameRepository.deckIsOver.value == true
    }

    private fun setupTableRecycler() {
        tableAdapter = TableAdapter()
        val rvTable: RecyclerView = findViewById(R.id.tableRecyclerView)
        rvTable.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTable.adapter = tableAdapter
        rvTable.addItemDecoration(SpacingItemDecoration(16))
    }
    private fun changeRoleCard(){
        serverManager.changeRoleCard(roomId, myId, object : ServerManager.ChangeRoleCallback {
            override fun onSuccess(updatedPlayer: String?) {
                Toast.makeText(this@MainActivityGame, "Роль сменена на: $updatedPlayer", Toast.LENGTH_SHORT).show()
            }
            override fun onError(errorMessage: String) {
                Toast.makeText(this@MainActivityGame, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("ОШИБКА СМЕНЫ КАРТЫ РОЛИ", "Текст: $errorMessage")
            }
        })
    }

    private fun changeMoodCard(){
        serverManager.changeMoodCard(roomId, myId, object : ServerManager.ChangeMoodCallback {
            override fun onSuccess(updatedPlayer: String?) {
                Toast.makeText(this@MainActivityGame, "Настроение сменено на: $updatedPlayer", Toast.LENGTH_SHORT).show()
            }
            override fun onError(errorMessage: String) {
                Toast.makeText(this@MainActivityGame, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("ОШИБКА СМЕНЫ КАРТЫ НАСТРОЕНИЯ", "Текст: $errorMessage")
            }
        })
    }

    private fun animateTextChange(textView: TextView, newText: String) {
        textView.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                textView.text = newText
                textView.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun stopGame(){
        if(!isCreator) return
        else{
            val stopGameButton: ImageButton = findViewById(R.id.closeButton)
            stopGameButton.setOnClickListener {
                WebSocketManager.stopGame(roomId,
                    onSuccess = { Toast.makeText(this, "Игра остановлена", Toast.LENGTH_SHORT).show() },
                    onError = { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() }
                )
            }
            finish()
        }

    }
}

class SpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.right = spacing
    }
}
