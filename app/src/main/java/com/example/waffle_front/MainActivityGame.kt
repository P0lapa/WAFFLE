package com.example.waffle_front

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivityGame : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        when(intent.getStringExtra("force_orientation")) {
            "landscape" -> requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
            "portrait" -> requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainGame)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val roomId = intent.getStringExtra("room_code") ?: "ОШИБКА: код не получен"
        val isCreator = intent.getBooleanExtra("is_creator", false)

        val showRoomId : View = findViewById(R.id.showRoomCodeLayout)
        val dimBackground: View = findViewById(R.id.dimBackground)
        val roomIdView : TextView = findViewById(R.id.roomCodeTextView)

        roomIdView.text = roomId


        if (isCreator) {
            showRoomId.visibility = View.VISIBLE
            // dimBackground.visibility = View.VISIBLE
        } else {
            showRoomId.visibility = View.GONE
            dimBackground.visibility = View.VISIBLE
        }

    }
}