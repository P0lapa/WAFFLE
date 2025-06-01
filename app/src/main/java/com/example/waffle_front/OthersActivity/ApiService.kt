package com.example.waffle_front.OthersActivity


import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.http.Body
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

