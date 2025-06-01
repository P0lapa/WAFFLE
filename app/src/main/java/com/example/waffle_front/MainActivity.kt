package com.example.waffle_front

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.waffle_front.OthersActivity.GameSettings
import com.example.waffle_front.OthersActivity.ServerManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@kotlinx.serialization.Serializable
data class Settings(val foo: String, val bar: Int)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Кнопки основного меню
        val playButton: Button = findViewById(R.id.playButton)
        val settingsButton: Button = findViewById(R.id.settingsButton)
        val rulesButton: Button = findViewById(R.id.rulesButton)
        val myCards: Button = findViewById(R.id.myCardsButton)
        val exitButton: Button = findViewById(R.id.exitButton)
        val infoButton: ImageButton = findViewById(R.id.infoButton)

        // Затемнение фона
        val dimBackground: View = findViewById(R.id.dimBackground)

        //Виджеты меню
        val createOrJoinGameLayout: View = findViewById(R.id.createOrJoinGameLayout)
        val settingsLayout: View = findViewById(R.id.settings)
        val infoLayout: View = findViewById(R.id.info)
        val rulesLayout: View = findViewById(R.id.rules)
        val createGameSettingsLayout: View = findViewById(R.id.createGameSettingsLayout)
        val joinGameLayout: View = findViewById(R.id.joinGameLayout)

        // Элементы кнопок НЕ основного меню
        val createGameButton: Button = findViewById(R.id.createGameButton)
        val joinGameButton: Button = findViewById(R.id.joinGameButton)
        val createGameSettingsButton: Button = findViewById(R.id.createGameSettingsButton)
        val findGameButton: Button = findViewById(R.id.findGameButton)

        // Обработчик для кнопки "Играть"
        playButton.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            createOrJoinGameLayout.visibility = View.VISIBLE
        }

        // Обработчик для кнопки "Настройки"
        settingsButton.setOnClickListener {
//            Toast.makeText(this, "Настройки", Toast.LENGTH_SHORT).show()
//            // TODO: Добавить логику для управления паками и добавленияя своих слов в паки
//            val intent = Intent(this, SettingsActivity::class.java)
//            startActivity(intent)
            dimBackground.visibility = View.VISIBLE
            settingsLayout.visibility = View.VISIBLE

            val closeRulesButton: Button = findViewById(R.id.closeRulesButton)
            closeRulesButton.setOnClickListener {
                dimBackground.visibility = View.GONE
                rulesLayout.visibility = View.GONE
            }
        }

         //Обработчик для кнопки "Правила"
        rulesButton.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            rulesLayout.visibility = View.VISIBLE
        }

         //Обработчик для кнопки "Info"
        infoButton.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            infoLayout.visibility = View.VISIBLE
        }

        // Обработчик для кнопки "Мои Карты"
        myCards.setOnClickListener {
            Toast.makeText(this, "Мои карты нажаты", Toast.LENGTH_SHORT).show()
            // TODO: Добавить логику для управления паками и добавленияя своих слов в паки ла-ла ла
            val intent = Intent(this, CardsActivity::class.java)
            startActivity(intent)
        }

        // Обработчик для кнопки "Выход"
        exitButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Да") { _, _ -> finish() }
                .setNegativeButton("Нет", null)
                .show()
        }


        fun readCardsFromFile(fileName: Int): List<String> {
            val cards = mutableListOf<String>()
            val inputStream = resources.openRawResource(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.useLines { lines ->
                lines.forEach { line ->
                    cards.add(line)
                }
            }
            return cards
        }


        joinGameButton.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            joinGameLayout.visibility=View.VISIBLE
        }

        createGameSettingsButton.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            createGameSettingsLayout.visibility=View.VISIBLE
        }

        createGameButton.setOnClickListener {
            val situationCards = readCardsFromFile(R.raw.situation_cards)
            val roleCards = readCardsFromFile(R.raw.role_cards)
            val moodCards = readCardsFromFile(R.raw.mood_cards)
            val actionCards = readCardsFromFile(R.raw.action_cards)
            val cardsInput: EditText = findViewById(R.id.cardsPerPlayerInput)
            val numberOfCards = cardsInput.text.toString().toIntOrNull() ?: 6

            val settings = GameSettings(
                creatorLogin = "Petya",
                cardsPerPlayer = numberOfCards,
                situationCards = situationCards,
                roleCards = roleCards,
                moodCards = moodCards,
                actionCards = actionCards
            )
            val jsonString = Json.encodeToString(settings)
            val fileName = "request.json"
            val file = File(getExternalFilesDir(null), fileName)
            file.writeText(jsonString)
            Log.d("JSON_DEBUG", "JSON файл сохранён в: ${file.absolutePath}")
            Log.d("JSON_CONTENT", jsonString)
            // Создаём комнату через ServerManager
            val serverManager = ServerManager(this)  // Передаём контекст Activity
            serverManager.createRoom(settings, object : ServerManager.RoomCreationCallback {
                override fun onSuccess(roomId: String) {
                    Toast.makeText(this@MainActivity, "Комната создана: $roomId", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, MainActivityGame::class.java).apply {
                        putExtra("room_code", roomId)
                        putExtra("is_creator", true)
                        putExtra("force_orientation", "landscape")
                    }
                    startActivity(intent)
                }

                override fun onError(errorMessage: String) {
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR", errorMessage)
                }
            })
        }

        findGameButton.setOnClickListener {
            val input: EditText = findViewById(R.id.roomNumber)
            val roomCode = input.text.toString()
            val login = "Volodya" // Можно заменить на реальный логин из EditText
            if (roomCode.isEmpty()) {
                Toast.makeText(this, "Введите номер комнаты", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val serverManager = ServerManager(this)
            serverManager.joinRoom(roomCode, login, object : ServerManager.JoinRoomCallback {
                override fun onSuccess(roomCode: String) {
                    Toast.makeText(
                        this@MainActivity,
                        "Вы присоединились к комнате $roomCode",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@MainActivity, MainActivityGame::class.java).apply {
                        putExtra("room_code", roomCode)
                        putExtra("force_orientation", "landscape")
                    }
                    startActivity(intent)
                }
                override fun onError(errorMessage: String) {
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("JOIN_ROOM", errorMessage)
                }
            })
        }


        dimBackground.setOnClickListener {
            dimBackground.visibility = View.GONE
            createOrJoinGameLayout.visibility = View.GONE
            settingsLayout.visibility=View.GONE
            rulesLayout.visibility=View.GONE
            infoLayout.visibility=View.GONE
            createGameSettingsLayout.visibility=View.GONE
            joinGameLayout.visibility=View.GONE
        }


    }
}