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

    private val _currentSituationCard = MutableLiveData<SituationCard>()
    val currentSituationCard: LiveData<SituationCard> = _currentSituationCard

    private val _deckIsOver = MutableLiveData<Boolean>(false)
    val deckIsOver: LiveData<Boolean> = _deckIsOver

    private val _droppedCards = MutableLiveData<List<ContentCard>>(emptyList())
    val droppedCards: LiveData<List<ContentCard>> = _droppedCards

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
                    val event = Json.decodeFromString<PlayerEnterEvent>(payload)
                    // создаём объект PlayerData, как у тебя раньше
                    val newPlayer = PlayerData(
                        playerId = event.body.playerId ?: "", // если сервер не шлёт ID, можно оставить пустым или взять логин
                        login = event.body.playerLogin,
                        roleCard = ContentCard("", "Новая роль"),
                        moodCard = ContentCard("", "Новое настроение"),
                        actionCards = emptyList()
                    )
                    // добавляем к текущему списку
                    _players.postValue(_players.value?.plus(newPlayer) ?: listOf(newPlayer))
                }
                "SITUATION_CARD_CHANGED" -> {
                    val event = Json.decodeFromString<SituationCardChangedEvent>(payload)
                    handleSituationCardChanged(event)
                }
                "ACTION_CARD_DROPPED" -> {
                    val event = Json.decodeFromString<ActionCardDroppedEvent>(payload)
                    val dropped = event.body.droppedCard

                    // 1) удаляем карту у всех игроков
                    val updatedPlayers = _players.value
                        ?.map { player ->
                            val newActions = player.actionCards.filter { it.id != dropped.id }
                            player.copy(actionCards = newActions)
                        }
                    _players.postValue(updatedPlayers ?: emptyList())

                    // 2) добавляем на “стол” и оставляем только последние 3
                    val current = _droppedCards.value ?: emptyList()
                    val newList = (current + dropped).takeLast(3)
                    _droppedCards.postValue(newList)
                }
                "DECK_IS_OVER" -> {
                    // установим флаг, чтобы все подписчики узнали, что колода пустая
                    _deckIsOver.postValue(true)
                }
                else -> Log.d("GameRepo", "Unknown event: $msg")

            }
        } catch (e: Exception) {
            Log.e("GameRepo", "Parse error: ${e.message}\nPayload: $payload")
        }
    }

    private fun handleSituationCardChanged(event: SituationCardChangedEvent) {
        _currentSituationCard.postValue(event.body.newCard)
        Log.d("GameRepo", "Situation card changed to: ${event.body.newCard.content}")
    }

    fun discardActionCard(playerLogin: String, cardId: String) {
        val updated = _players.value
            ?.map { player ->
                if (player.login == playerLogin) {
                    val newActions = player.actionCards.filter { it.id != cardId }
                    player.copy(actionCards = newActions)
                } else player
            }
        _players.postValue(updated ?: emptyList())
    }

    fun replacePlayerData(newPlayer: PlayerData) {
        val updatedList = _players.value
            ?.map { existing ->
                if (existing.login == newPlayer.login) newPlayer else existing
            }
            ?: listOf(newPlayer)
        _players.postValue(updatedList)
    }
}

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

@Serializable
data class SituationCardChangedEvent(
    val message: String,
    val body: SituationCardChangedBody
)

@Serializable
data class SituationCardChangedBody(
    val newCard: SituationCard
)
@Serializable
data class PlayerEnterEvent(
    val message: String,
    val body: PlayerEnterBody
)

@Serializable
data class PlayerEnterBody(
    val playersCount: Int,
    val playerLogin: String,
    val playerId: String? = null
)

@Serializable
data class ActionCardDroppedEvent(
    val message: String,
    val body: ActionCardDroppedBody
)

@Serializable
data class ActionCardDroppedBody(
    val droppedCard: ContentCard
)