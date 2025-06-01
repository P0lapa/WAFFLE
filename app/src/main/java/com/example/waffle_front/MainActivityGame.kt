// MainActivityGame.kt
package com.example.waffle_front

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.waffle_front.OthersActivity.GameRepository
import com.example.waffle_front.OthersActivity.PlayerData
import com.example.waffle_front.OthersActivity.SituationCard
import com.example.waffle_front.OthersActivity.WebSocketManager

class MainActivityGame : AppCompatActivity() {
    // Добавляем адаптер
//    private val playersAdapter = PlayersAdapter()
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        when (intent.getStringExtra("force_orientation")) {
            "landscape" -> requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
//            "portrait"  -> requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainGame)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        val roomId = intent.getStringExtra("room_code") ?: return
        val isCreator = intent.getBooleanExtra("is_creator", false)

        val roomCodeView: TextView = findViewById(R.id.roomCodeTextView)
        roomCodeView.text = roomId

        val startBtn: Button = findViewById(R.id.startGameButton)
//        startBtn.isEnabled = false

        // Инициализация RecyclerView
//        val playersRecyclerView = findViewById<RecyclerView>(R.id.playersRecyclerView)
//        playersRecyclerView.layoutManager = LinearLayoutManager(this)
//        playersRecyclerView.adapter = playersAdapter

        WebSocketManager.connect(roomId) {
            startBtn.isEnabled = true
        }

        GameRepository.players.observe(this) { players ->
            updatePlayersUI(players)
            // Обновляем адаптер
//            playersAdapter.submitList(players)
        }

        GameRepository.gameStarted.observe(this) { event ->
            Toast.makeText(this, "Игра началась!", Toast.LENGTH_SHORT).show()
            showSituationCard(event.body.situationCard)
            val dimBackground: View = findViewById(R.id.dimBackground)
            val showRoomCodeLayout:ConstraintLayout  = findViewById(R.id.showRoomCodeLayout)
            dimBackground.visibility = View.GONE
            showRoomCodeLayout.visibility = View.GONE

            // Теперь не нужно обновлять игроков здесь,
            // так как это делается в handleWebSocketEvent
        }

        if (isCreator) {
            findViewById<View>(R.id.showRoomCodeLayout).visibility = View.VISIBLE
            startBtn.setOnClickListener {
                if (!WebSocketManager.isConnected()) {
                    Toast.makeText(this, "Соединение НЕ установлено!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                WebSocketManager.sendStartMessage(
                    onSuccess = { Toast.makeText(this, "Игра началась!", Toast.LENGTH_SHORT).show() },
                    onError   = { e -> Toast.makeText(this, e, Toast.LENGTH_SHORT).show() }
                )
            }
        } else {
            findViewById<View>(R.id.showRoomCodeLayout).visibility = View.GONE
        }

        val dimBackgroundMain: View = findViewById(R.id.dimBackgroundMain)
        val situationCardModal: LinearLayout = findViewById(R.id.situationCardModal) //модальное окно для карты ситуации
        val situationCardButton: ImageButton = findViewById(R.id.situationCardButton)
//        val closeModalButton: Button = findViewById(R.id.closeModalButton)
        // В коде Activity:

        dimBackgroundMain.setOnClickListener {
            dimBackgroundMain.visibility = View.GONE
            situationCardModal.visibility = View.GONE
        }

        situationCardButton.setOnClickListener {
            dimBackgroundMain.visibility = View.VISIBLE
            situationCardModal.visibility = View.VISIBLE
        }

//        closeModalButton.setOnClickListener {
//            dimBackground.visibility = View.GONE
//            situationCardModal.visibility = View.GONE
//        }
//
//        if (isCreator) {
//            changeSituationButton.visibility = View.VISIBLE
//            changeSituationButton.setOnClickListener {
//                // Логика смены карточки
//            }
//        }
    }

    override fun onDestroy() {
        WebSocketManager.disconnect()
        super.onDestroy()
    }

    private fun showSituationCard(card: SituationCard) {
        Log.d("GAME_DEBUG", """
    Situation Card:
    ID: ${card.id}
    Content: "${card.content}"
    ===================================
    """.trimIndent())

        val situationCard: TextView = findViewById(R.id.situationText)
        val situationCardModal: TextView = findViewById(R.id.situationTextModal)
        situationCard.text = card.content
        situationCardModal.text = card.content
    }

    private fun updatePlayersUI(players: List<PlayerData>) {
        Log.d("PLAYERS_UPDATE", "Получено ${players.size} игроков")
        players.forEachIndexed { index, player ->
            Log.d("PLAYER_${index + 1}", """
                Логин: ${player.login}
                Роль: ${player.roleCard.content}
                Настроение: ${player.moodCard.content}
                Действий: ${player.actionCards.size}
            """.trimIndent())
        }
    }

}
