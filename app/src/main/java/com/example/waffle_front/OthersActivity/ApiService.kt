package com.example.waffle_front.OthersActivity


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/rooms")
    fun createRoom(@Body settings: GameSettings): Call<CreateRoomResponse>

    @Headers("Content-Type: application/json")
    @POST("/rooms/{roomId}/players")
    fun joinRoom(
        @Path("roomId") roomId: String,
        @Body login: String
    ): Call<PlayerResponse>

    @POST("/rooms/{roomId}/cards/situation/change")
    fun changeSituationCard(
        @Path("roomId") roomId: String
    ): Call<SituationChangeResponse> //Возвращает 1, хотя фронт не умеет обрабатывать такое

    @GET("/rooms/{roomId}")
    fun getRoomState(
        @Path("roomId") roomId: String
    ): Call<RoomStateResponse>

    @POST("/rooms/{roomId}/players/{playerId}/cards/action/drop/{cardId}")
    fun dropActionCard(
        @Path("roomId") roomId: String,
        @Path("playerId") playerId: String,
        @Path("cardId") cardId: String
    ): Call<Boolean>

    @POST("/rooms/{roomId}/players/{playerId}/cards/action/draw")
    fun drawActionCard(
        @Path("roomId") roomId: String,
        @Path("playerId") playerId: String
    ): Call<Player>

//    @GET("/rooms/{roomId}/players/{playerId}/cards/role/change") Call<PlayerResponse>
//    @GET("/rooms/{roomId}/players/{playerId}/cards/mood/change") Call<PlayerResponse>

}

@Serializable
data class CreateRoomResponse(val roomId: String)

//Для запроса на создание комнаты
@Serializable
data class GameSettings(
    val creatorLogin: String,
    val cardsPerPlayer: Int,
    val situationCards: List<String>,
    val roleCards: List<String>,
    val moodCards: List<String>,
    val actionCards: List<String>
    //если ещё какие поля должны быть в json добавлять сюда
)


// Класс для ответа сервера при успешном присоединении
@Serializable
data class PlayerResponse(
    val playerId: String,
    val login: String,
    val roleCards: List<String>? = null,
    val moodCards: List<String>? = null,
    val actionCards: List<String>? = null
)
//Смена карты ситуации
@Serializable
data class SituationChangeResponse(
    val success: Boolean
)

@Serializable
data class RoomStateResponse(
    @SerialName("roomId") val roomId: String,
    @SerialName("creatorId") val creatorId: String,
    @SerialName("cardsPerPlayer") val cardsPerPlayer: Int,
    @SerialName("situationCard") val situationCard: Card? = null,
    @SerialName("players") val players: List<Player> = emptyList(),
    @SerialName("situationCards") val availableSituations: List<Card>? = null,
    @SerialName("roleCards") val availableRoles: List<Card>? = null,
    @SerialName("moodCards") val availableMoods: List<Card>? = null,
    @SerialName("actionCards") val availableActions: List<Card>? = null,
    @SerialName("droppedActionCards") val discardedActions: List<Card>? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("isGameStarted") val isGameStarted: Boolean = false
)


@Serializable
data class Player(
    @SerialName("playerId") val id: String,
    @SerialName("login") val login: String,
    @SerialName("roleCard") val role: Card? = null,
    @SerialName("moodCard") val mood: Card? = null,
    @SerialName("actionCards") val actions: List<Card>? = null // <- nullable
)


@Serializable
data class Card(
    @SerialName("content") val text: String,
    @SerialName("id") val id: String
)

