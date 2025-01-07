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
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.waffle_front.OthersActivity.GameSettings
import com.example.waffle_front.OthersActivity.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.BufferedReader
import java.io.InputStreamReader

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

        // Элементы обработки кнопки "Играть"
        val playOptions: View = findViewById(R.id.playOptions)
        val createGameButton: Button = findViewById(R.id.createGameButton)
        val joinGameButton: Button = findViewById(R.id.joinGameButton)


        // Обработчик для кнопки "Играть"
        playButton.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            playOptions.visibility = View.VISIBLE
        }

        // Обработчик для кнопки "Настройки"
        settingsButton.setOnClickListener {
            Toast.makeText(this, "Настройки нажаты", Toast.LENGTH_SHORT).show()
            // TODO: Добавить логику для открытия экрана настроек
        }

        // Обработчик для кнопки "Правила"
        rulesButton.setOnClickListener {
            Toast.makeText(this, "Правила нажаты", Toast.LENGTH_SHORT).show()
            // TODO: Добавить логику для открытия экрана с правилами
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

        // Обработчик для кнопки "Info"
        infoButton.setOnClickListener {
            Toast.makeText(this, "Информация нажата", Toast.LENGTH_SHORT).show()
            // TODO: Добавить логику для показа информации или помощи
        }

        infoButton.setOnClickListener {
            Toast.makeText(this, "Информация нажата", Toast.LENGTH_SHORT).show()
            // TODO: Добавить логику для показа информации или помощи
        }

        fun readCardsFromFile(fileName: String): List<String> {
            val cards = mutableListOf<String>()
            val inputStream = assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.useLines { lines ->
                lines.forEach { line ->
                    cards.add(line)
                }
            }
            return cards
        }

        createGameButton.setOnClickListener {
            // Логика для создания игры
            val situationCards = readCardsFromFile("situation_cards.txt")
            val roleCards = readCardsFromFile("role_cards.txt")
            val moodCards = readCardsFromFile("mood_cards.txt")
            val actionCards = readCardsFromFile("action_cards.txt")

            val settings = GameSettings(
                creatorLogin = "user123",
                cardsPerPlayer = 6,
                situationCards = situationCards,
                roleCards = roleCards,
                moodCards = moodCards,
                actionCards = actionCards
            )

            val api = RetrofitClient.instance
            api.createRoom(settings).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Комната создана", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Ошибка создания комнаты", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Ошибка: ${t.message}",Toast.LENGTH_SHORT).show()
                }
            })
        }

        joinGameButton.setOnClickListener {
            // Логика для присоединения к игре
        }

        dimBackground.setOnClickListener {
            dimBackground.visibility = View.GONE
            playOptions.visibility = View.GONE
        }


    }
}
