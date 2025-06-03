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
import android.widget.ImageButton
import android.widget.LinearLayout
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
//    private lateinit var cardsPerPlayer: Int
    private lateinit var playersAdapter: PlayersAdapter
    private lateinit var actionsAdapter: ActionCardsAdapter
    private lateinit var serverManager: ServerManager
    private lateinit var tableAdapter: TableAdapter

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
        val isCreator = intent.getBooleanExtra("is_creator", false)

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
        } else {
            findViewById<View>(R.id.showRoomCodeLayout).visibility = View.VISIBLE
            findViewById<View>(R.id.showRoomCodeWidget).visibility = View.GONE
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            myLogin = prefs.getString("my-login", "") ?: ""
            myId =prefs.getString("my-id", "") ?: ""
            observePlayersAfterLogin()
        }

        setupPlayersRecycler() // инициализация RecyclerView и адаптер
        setupActionsRecycler() // инициализация RecyclerView и адаптер
        setupTableRecycler() // инициализация RecyclerView и адаптер

        GameRepository.droppedCards.observe(this) { dropped ->
            tableAdapter.submitList(dropped)
        }


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
        val situationCardModal: LinearLayout = findViewById(R.id.situationCardModal) //модальное окно для карты ситуации
        val situationCardButton: ImageButton = findViewById(R.id.situationCardButton)
        val changesituationButton: ImageButton = findViewById(R.id.changeSituationButton)


        dimBackgroundMain.setOnClickListener {
            dimBackgroundMain.visibility = View.GONE
            situationCardModal.visibility = View.GONE
            changesituationButton.visibility = View.GONE
        }

        situationCardButton.setOnClickListener {
            dimBackgroundMain.visibility = View.VISIBLE
            situationCardModal.visibility = View.VISIBLE
            changesituationButton.visibility = View.VISIBLE
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
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                })
        }


        setupSituationCardObserver()
//        observePlayersAfterLogin()
//        setupPlayersRecycler()
//        setupPlayersObserver()
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

        val situationCard: TextView = findViewById(R.id.situationText)
        val situationCardModal: TextView = findViewById(R.id.situationTextModal)
        situationCard.text = card.content
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

    private fun observePlayersAfterLogin() {
        GameRepository.players.observe(this) { players ->
            val others = players.filter { it.login != myLogin }
            playersAdapter.submitList(others)

            val me = players.firstOrNull { it.login == myLogin }
            val myCards = me?.actionCards ?: emptyList()
            actionsAdapter.submitList(myCards)

            if (others.isNotEmpty()) {
                Toast.makeText(this, "Игрок ${others.last().login} присоединился!", Toast.LENGTH_SHORT).show()
            }

            val currentActions = me?.actionCards
            if (currentActions != null) {
                actionsAdapter.submitList(currentActions)
            }

            val desiredCount = 6//prefs.getInt("cards-per-player", 6)
            if (currentActions != null) {
                if (currentActions.size < desiredCount && !isDeckOver()) {
                    drawNewCardForMe()
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
                Toast.makeText(this@MainActivityGame, errorMessage, Toast.LENGTH_SHORT).show()
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
