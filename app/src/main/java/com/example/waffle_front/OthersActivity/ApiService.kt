package com.example.waffle_front.OthersActivity

// ApiService.kt
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/rooms")
    fun createRoom(@Body settings: GameSettings): Call<CreateRoomResponse>
}

data class CreateRoomResponse(
    val roomId: String
)

data class GameSettings(
    val creatorLogin: String,
    val cardsPerPlayer: Int,
    val situationCards: List<String>,
    val roleCards: List<String>,
    val moodCards: List<String>,
    val actionCards: List<String>
    //если ещё какие поля должны быть в json добавлять сюда
)

