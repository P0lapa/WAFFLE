package com.example.waffle_front.OthersActivity

import android.content.Context
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServerManager(private val context: Context) {  // Передаём Context в конструктор

    interface RoomCreationCallback {
        fun onSuccess(roomId: String)
        fun onError(errorMessage: String)
    }

    interface JoinRoomCallback {
        fun onSuccess(roomId: String)
        fun onError(errorMessage: String)
    }

    interface ChangeSituationCallback {
        fun onSuccess(successMessage: String)
        fun onError(errorMessage: String)
    }

    fun createRoom(settings: GameSettings, callback: RoomCreationCallback) {
        val api = RetrofitClient.instance
        api.createRoom(settings).enqueue(object : Callback<CreateRoomResponse> {
            override fun onResponse(
                call: Call<CreateRoomResponse>,
                response: Response<CreateRoomResponse>
            ) {
                if (response.isSuccessful) {
                    val roomId = response.body()?.roomId ?: run {
                        callback.onError("Не удалось получить roomId")
                        return
                    }
                    callback.onSuccess(roomId)
                } else {
                    callback.onError("Ошибка: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CreateRoomResponse>, t: Throwable) {
                callback.onError("Ошибка сети: ${t.message}")
            }
        })
    }

    fun joinRoom(roomId: String, login: String, callback: JoinRoomCallback){
//        val roomId = intent.getStringExtra("room_code") ?: "ОШИБКА: код не получен"
//        val isCreator = intent.getBooleanExtra("is_creator", false)
        //webSocket подключение
//        roomIdView.text = roomId
//        val client = StompClient()
//        client.connect(roomId)
        //http подключение

        val api = RetrofitClient.instance
        api.joinRoom(roomId, login).enqueue(object : Callback<PlayerResponse> {
            override fun onResponse(call: Call<PlayerResponse>, response: Response<PlayerResponse>) {
                if (response.isSuccessful) {
                    val playerResponse = response.body()
                    if (playerResponse != null) {
                        callback.onSuccess(roomId)
                    } else {
                        callback.onError("Пустой ответ сервера")
                    }
                } else {
                    callback.onError("Ошибка: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PlayerResponse>, t: Throwable) {
                callback.onError("Ошибка сети: ${t.message}")
            }
        })
    }

    fun changeSituationCard(roomId: String, callback: ChangeSituationCallback) {
        val api = RetrofitClient.instance
        api.changeSituationCard(roomId).enqueue(object : Callback<SituationChangeResponse> {
            override fun onResponse(
                call: Call<SituationChangeResponse>,
                response: Response<SituationChangeResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        callback.onSuccess("Карта ситуации была сменена")
                    } else {
                        callback.onError("Карта ситуации НЕ была сменена")
                    }
                } else {
                    callback.onError("Ошибка: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<SituationChangeResponse>, t: Throwable) {
                callback.onError("Ошибка сети: ${t.message}")
            }
        })
    }
}