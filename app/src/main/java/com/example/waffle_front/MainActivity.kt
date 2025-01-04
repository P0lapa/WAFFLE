package com.example.waffle_front

import StrokeSpan
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Найти кнопки по ID
        val playButton: Button = findViewById(R.id.playButton)
        val settingsButton: Button = findViewById(R.id.settingsButton)
        val rulesButton: Button = findViewById(R.id.rulesButton)
        val exitButton: Button = findViewById(R.id.exitButton)
        val infoButton: ImageButton = findViewById(R.id.infoButton)

        // Обработчик для кнопки "Играть"
        playButton.setOnClickListener {
            Toast.makeText(this, "Играть нажато", Toast.LENGTH_SHORT).show()
            // TODO: Добавить логику для открытия экрана игры
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

        // Обработчик для кнопки "Выход"
        exitButton.setOnClickListener {
            Toast.makeText(this, "Выход нажат", Toast.LENGTH_SHORT).show()
            // TODO: Реализовать завершение приложения
            finish()
        }

        // Обработчик для кнопки "Info"
        infoButton.setOnClickListener {
            Toast.makeText(this, "Информация нажата", Toast.LENGTH_SHORT).show()
            // TODO: Добавить логику для показа информации или помощи
        }

    }
}
