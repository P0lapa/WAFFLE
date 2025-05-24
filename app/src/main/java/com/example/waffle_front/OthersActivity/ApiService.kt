package com.example.waffle_front.OthersActivity

// ApiService.kt
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/rooms")
    fun createRoom(@Body settings: GameSettings): Call<String>

    @Headers("Content-Type: application/json")
    @POST("/rooms/{roomId}/players")
    fun joinRoom(
        @Path("roomId") roomId: String,
        @Body login: String
    ): Call<PlayerResponse> // Предположим, что сервер возвращает информацию о игроке

//    @POST("/rooms/{roomId}/start ") //начать игру
//    fun startGame(
//        @Path("/start")
//    )

}

data class CreateRoomResponse(
    val roomId: String
)

//Для запроса на создание комнаты
data class GameSettings(
    val creatorLogin: String,
    val cardsPerPlayer: Int,
    val situationCards: List<String>,
    val roleCards: List<String>,
    val moodCards: List<String>,
    val actionCards: List<String>
    //если ещё какие поля должны быть в json добавлять сюда
)
git
// Класс для ответа сервера при успешном присоединении
data class PlayerResponse(
    val playerId: String,
    val login: String,
    val roomId: String
)