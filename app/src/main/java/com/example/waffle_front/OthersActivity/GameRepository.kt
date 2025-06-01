// GameRepository.kt
package com.example.waffle_front.OthersActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object GameRepository {
    private val _players = MutableLiveData<List<PlayerData>>(emptyList())
    val players: LiveData<List<PlayerData>> = _players

    private val _gameStarted = MutableLiveData<GameStartedEvent>()
    val gameStarted: LiveData<GameStartedEvent> = _gameStarted

    fun handleWebSocketEvent(payload: String) {
        try {
            val root = Json.parseToJsonElement(payload).jsonObject
            val msg = root["message"]?.jsonPrimitive?.content

            when (msg) {
                "GAME_STARTED" -> {
                    val event = Json.decodeFromString<GameStartedEvent>(payload)
                    _gameStarted.postValue(event)
                    // Обновляем список игроков из события начала игры
                    _players.postValue(event.body.players)
                }
                "PLAYER_ENTER" -> {
                    // ВАЖНО: Теперь нужно парсить полные данные игрока
                    val event = Json.decodeFromString<PlayerEnterEvent>(payload)
                    val newPlayer = event.body.playerData // Используем новый класс
                    _players.postValue(_players.value?.plus(newPlayer) ?: listOf(newPlayer))
                }
                else -> Log.d("GameRepo", "Unknown event: $msg")
            }
        } catch (e: Exception) {
            Log.e("GameRepo", "Parse error: ${e.message}\nPayload: $payload")
        }
    }
}

// Новый класс для события входа игрока
@Serializable
data class PlayerEnterEvent(
    val message: String,
    val body: PlayerEnterBody
)

@Serializable
data class PlayerEnterBody(
    val playerData: PlayerData, // Теперь передаем полные данные игрока
    val playersCount: Int
)


@Serializable
data class GameStartedEvent(
    val message: String,
    val body: GameStartedBody
)

@Serializable
data class GameStartedBody(
    val situationCard: SituationCard,
    val players: List<PlayerData>
)

@Serializable
data class SituationCard(
    val id: String,
    val content: String
)

@Serializable
data class PlayerData(
    val playerId: String,
    val login: String,
    val roleCard: ContentCard,
    val moodCard: ContentCard,
    val actionCards: List<ContentCard>
)

@Serializable
data class ContentCard(
    val id: String,
    val content: String
)