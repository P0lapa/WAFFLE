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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waffle_front.OthersActivity.CardSet
import com.example.waffle_front.OthersActivity.CardSetAdapter
import com.example.waffle_front.OthersActivity.GameSettings
import com.example.waffle_front.OthersActivity.ServerManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
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

        // Элементы выбора наборов для игры
        val recyclerSituations = findViewById<RecyclerView>(R.id.recyclerSituations)
        val recyclerMoods = findViewById<RecyclerView>(R.id.recyclerMoods)
        val recyclerRoles = findViewById<RecyclerView>(R.id.recyclerRoles)
        val recyclerActions = findViewById<RecyclerView>(R.id.recyclerActions)

        // Списки индексов выбраных наборов для игры
        val selectedSituationsGroups = mutableListOf<Int>()
        val selectedMoodsGroups = mutableListOf<Int>()
        val selectedRolesGroups = mutableListOf<Int>()
        val selectedActionsGroups = mutableListOf<Int>()


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


        fun readCardsFromFile(baseFileName: Int, fileName: String, groupsIndexes: List<Int>): List<String> {
            val cards = mutableListOf<String>()
            val file = File(filesDir, fileName)
            if (file.exists()){
                try {
                    val inputStream = openFileInput(fileName)
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    inputStream.close()

                    val jsonObject = JSONObject(jsonString)
                    val allGroups = jsonObject.keys().asSequence().toList()

                    val indexesToSelect = if (groupsIndexes.isEmpty()) allGroups.indices.toList() else groupsIndexes

                    indexesToSelect.forEach { index ->
                        if (index in allGroups.indices) {
                            val group = allGroups[index]
                            val jsonArray = jsonObject.getJSONArray(group)

                            for (i in 0 until jsonArray.length()) {
                                cards.add(jsonArray.getString(i))
                            }
                        }
                    }
//                    Toast.makeText(this, "Выбранные данные успешно загружены из $fileName", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка при загрузке данных: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
            else{
                val inputStream = resources.openRawResource(baseFileName)
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.useLines { lines ->
                    lines.forEach { line ->
                        cards.add(line)
                    }
                }
            }
            return cards
        }


        joinGameButton.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            joinGameLayout.visibility=View.VISIBLE
        }

        fun loadGroupsFromJson(fileName: String): List<String> {
            val groups = mutableListOf<String>()
            val file = File(filesDir, fileName)
            if (file.exists()){
                try {
                    val inputStream = openFileInput(fileName)
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    inputStream.close()

                    val jsonObject = JSONObject(jsonString)

                    jsonObject.keys().forEach { group ->
                        groups.add(group) // Добавляем название группы в список
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка при загрузке групп: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
            else {
                groups.add("Базовый набор")
            }


            return groups
        }

        createGameSettingsButton.setOnClickListener {
            val situationGroups = loadGroupsFromJson("situation_cards.json")
            val moodGroups = loadGroupsFromJson("mood_cards.json")
            val roleGroups = loadGroupsFromJson("role_cards.json")
            val actionGroups = loadGroupsFromJson("action_cards.json")

            val situations = situationGroups.map { CardSet(it, true) }
            val moods = moodGroups.map { CardSet(it, true) }
            val roles = roleGroups.map { CardSet(it, true) }
            val actions = actionGroups.map { CardSet(it, true) }

            val adapterSituations = CardSetAdapter(situations) { index, isSelected ->
                if (isSelected) {
                    if (!selectedSituationsGroups.contains(index)) selectedSituationsGroups.add(index)
                } else {
                    selectedSituationsGroups.remove(index)
                }
            }
            val adapterMoods = CardSetAdapter(moods) { index, isSelected ->
                if (isSelected) {
                    if (!selectedMoodsGroups.contains(index)) selectedMoodsGroups.add(index)
                } else {
                    selectedMoodsGroups.remove(index)
                }
            }
            val adapterRoles = CardSetAdapter(roles) { index, isSelected ->
                if (isSelected) {
                    if (!selectedRolesGroups.contains(index)) selectedRolesGroups.add(index)
                } else {
                    selectedRolesGroups.remove(index)
                }
            }
            val adapterActions = CardSetAdapter(actions) { index, isSelected ->
                if (isSelected) {
                    if (!selectedActionsGroups.contains(index)) selectedActionsGroups.add(index)
                } else {
                    selectedActionsGroups.remove(index)
                }
            }

            recyclerSituations.adapter = adapterSituations
            recyclerSituations.layoutManager = LinearLayoutManager(this)

            recyclerMoods.adapter = adapterMoods
            recyclerMoods.layoutManager = LinearLayoutManager(this)

            recyclerRoles.adapter = adapterRoles
            recyclerRoles.layoutManager = LinearLayoutManager(this)

            recyclerActions.adapter = adapterActions
            recyclerActions.layoutManager = LinearLayoutManager(this)

            dimBackground.visibility = View.VISIBLE
            createGameSettingsLayout.visibility=View.VISIBLE
        }

        createGameButton.setOnClickListener {
            val situationCards = readCardsFromFile(R.raw.situation_cards, "situation_cards.json", selectedSituationsGroups)
            val roleCards = readCardsFromFile(R.raw.role_cards, "role_cards.json", selectedRolesGroups)
            val moodCards = readCardsFromFile(R.raw.mood_cards, "mood_card.json", selectedMoodsGroups)
            val actionCards = readCardsFromFile(R.raw.action_cards, "action_cards.json", selectedActionsGroups)
            val cardsInput: EditText = findViewById(R.id.cardsPerPlayerInput)
            val numberOfCards = cardsInput.text.toString().toIntOrNull() ?: 6

            val nickName: EditText = findViewById(R.id.nickName)
            val login: String? = nickName.text.toString().takeIf { it.isNotBlank() }
//            val enteredLogin:

            if (login == null) {
                Toast.makeText(this, "Введите nickname", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val settings = GameSettings(
                creatorLogin = "",
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
                    nickName.text.clear()
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
            val nickNameToJoin: EditText = findViewById(R.id.nickName)
            val login: String? = nickNameToJoin.text.toString().takeIf { it.isNotBlank() }
//            val enteredLogin:
            if (roomCode.isEmpty()) {
                Toast.makeText(this, "Введите номер комнаты", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (login == null) {
                Toast.makeText(this, "Введите nickname", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val serverManager = ServerManager(this)
            serverManager.joinRoom(this, roomCode, login, object : ServerManager.JoinRoomCallback {
                override fun onSuccess(roomId: String) {
                    Toast.makeText(
                        this@MainActivity,
                        "Вы присоединились к комнате $roomId",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@MainActivity, MainActivityGame::class.java).apply {
                        putExtra("room_code", roomId)
                        putExtra("force_orientation", "landscape")
                    }
                    nickNameToJoin.text.clear()
                    input.text.clear()
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