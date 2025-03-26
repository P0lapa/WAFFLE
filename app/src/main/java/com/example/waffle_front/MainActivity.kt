package com.example.waffle_front
import StrokeSpan
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.waffle_front.OthersActivity.CreateRoomResponse
import com.example.waffle_front.OthersActivity.GameSettings
import com.example.waffle_front.OthersActivity.PlayerResponse
import com.example.waffle_front.OthersActivity.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import com.google.gson.Gson


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
            dimBackground.visibility = View.VISIBLE
            settingsLayout.visibility = View.VISIBLE
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
            // TODO: Добавить логику для управления паками и добавленияя своих слов в паки
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
            // Логика для создания игры
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

            val api = RetrofitClient.instance


// Генерация JSON
            val jsonString = Gson().toJson(settings)

// Сохранение файла в локальное хранилище эмулятора
            val fileName = "request.json"
            val file = File(getExternalFilesDir(null), fileName)
            file.writeText(jsonString)

// Логируем путь для удобства
            Log.d("JSON_DEBUG", "JSON файл сохранён в: ${file.absolutePath}")

// Логируем содержимое файла
            Log.d("JSON_CONTENT", jsonString)
            Toast.makeText(this@MainActivity, "Файл залогирован", Toast.LENGTH_SHORT).show()
            api.createRoom(settings).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val roomId = response.body() // Получаем строку напрямую
                        Log.i("Номер комнаты:", roomId.toString())
                        Toast.makeText(this@MainActivity, "Номер комнаты: $roomId", Toast.LENGTH_SHORT).show()
                        //запуск окна игры
                        val intent = Intent(this@MainActivity, MainActivityGame::class.java)
                        intent.putExtra("room_code", roomId)
                        intent.putExtra("force_orientation", "landscape")
                        startActivity(intent)
                    } else {
                        Log.e("CreateRoom", "Ошибка создания комнаты. Ответ: ${response.code()} ${response.message()}")
                        Toast.makeText(this@MainActivity, "Ошибка создания комнаты", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
                    println("Ошибка: ${t.message}")
                }
            })
        }

        findGameButton.setOnClickListener {
            val input: EditText = findViewById(R.id.roomNumber)
            val roomCode = input.text.toString() // Получаем roomId как строку

            val login = "" // Логин игрока (можете получить его из другого поля ввода)
            val api = RetrofitClient.instance
            // Вызов метода для присоединения к комнате
            api.joinRoom(roomCode, login).enqueue(object : Callback<PlayerResponse> {
                override fun onResponse(call: Call<PlayerResponse>, response: Response<PlayerResponse>) {
                    if (response.isSuccessful) {
                        val playerResponse = response.body()
                        if (playerResponse != null) {
                            Log.i("JOIN_ROOM", "Игрок успешно присоединился к комнате: ${playerResponse.roomId}")
                            Toast.makeText(this@MainActivity, "Вы присоединились к комнате ${playerResponse.roomId}", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("JOIN_ROOM", "Ответ сервера пустой")
                            Toast.makeText(this@MainActivity, "Ошибка: пустой ответ сервера", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("JOIN_ROOM", "Ошибка присоединения к комнате. Код: ${response.code()}, Сообщение: ${response.message()}")
                        Toast.makeText(this@MainActivity, "Ошибка присоединения к комнате", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PlayerResponse>, t: Throwable) {
                    Log.e("JOIN_ROOM", "Ошибка: ${t.message}")
                    Toast.makeText(this@MainActivity, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
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
