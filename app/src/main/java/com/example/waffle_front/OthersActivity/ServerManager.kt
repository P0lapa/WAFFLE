package com.example.waffle_front.OthersActivity

import android.content.Context
import android.util.Log
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

    interface HttpCallback {
        fun onSuccess(successMessage: String)
        fun onError(errorMessage: String)
    }

    interface DropActionCallback {
        fun onSuccess()
        fun onError(errorMessage: String)
    }

    interface DrawActionCallback {
        fun onSuccess(newPlayerData: PlayerData)
        fun onError(errorMessage: String)
    }

    interface ChangeRoleCallback {
        fun onSuccess(updatedPlayer: String?)
        fun onError(errorMessage: String)
    }

    interface ChangeMoodCallback {
        fun onSuccess(updatedPlayer: String?)
        fun onError(errorMessage: String)
    }

    interface LeaveRoomCallback {
        fun onSuccess()
        fun onError(errorMessage: String)
    }

    interface StopGameCallback {
        fun onSuccess()
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

    fun joinRoom(context: Context, roomId: String, login: String, callback: JoinRoomCallback) {
        val api = RetrofitClient.instance
        api.joinRoom(roomId, login).enqueue(object : Callback<PlayerResponse> {
            override fun onResponse(call: Call<PlayerResponse>, response: Response<PlayerResponse>) {
                if (response.isSuccessful) {
                    val playerResponse = response.body()
                    if (playerResponse != null) {
                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("my-login", playerResponse.login).apply()
                        prefs.edit().putString("my-id", playerResponse.playerId).apply()
                        callback.onSuccess(roomId)
                    } else {
                        callback.onError("пустой ответ сервера")
                    }
                } else {
                    callback.onError("ошибка: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PlayerResponse>, t: Throwable) {
                callback.onError("ошибка сети: ${t.message}")
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

    fun getCreatorLogin(
        context: Context,
        roomId: String,
        callback: HttpCallback
    ) {
        val api = RetrofitClient.instance
        api.getRoomState(roomId).enqueue(object : Callback<RoomStateResponse> {
            override fun onResponse(
                call: Call<RoomStateResponse>,
                response: Response<RoomStateResponse>
            ) {
                if (!response.isSuccessful) {
                    callback.onError("ошибка: ${response.code()} ${response.message()}")
                    return
                }
                val body = response.body()
                if (body == null) {
                    callback.onError("пустой ответ сервера")
                    return
                }
                // достаём creatorId из ответа
                val creatorId = body.creatorId
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putInt("cards-per-player", body.cardsPerPlayer)
                    .putString("creator-id", creatorId)
                    .apply()
                // ищем в списке players того, чей id == creatorId
                val creatorPlayer = body.players.firstOrNull { it.id == creatorId }
                if (creatorPlayer == null) {
                    callback.onError("не найден создатель комнаты в списке игроков")
                    return
                }
                val creatorLogin = creatorPlayer.login

                // сохраняем в SharedPreferences

                prefs.edit()
                    .putString("creator-login", creatorLogin)
                    .putString("creator-id", creatorId)
                    .apply()

                callback.onSuccess("логин создателя сохранён: $creatorLogin")
            }

            override fun onFailure(call: Call<RoomStateResponse>, t: Throwable) {
                callback.onError("ошибка сети: ${t.message}")
            }
        })
    }

    fun dropActionCard(
        roomId: String,
        playerId: String,
        cardId: String,
        callback: DropActionCallback
    ) {
        val api = RetrofitClient.instance
        api.dropActionCard(roomId, playerId, cardId).enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful && response.body() == true) {
                    callback.onSuccess()
                } else {
                    callback.onError("Не удалось сбросить карту. Код: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                callback.onError("Ошибка сети: ${t.message}")
            }
        })
    }

    fun drawActionCard(
        roomId: String,
        playerId: String,
        callback: DrawActionCallback
    ) {
        val api = RetrofitClient.instance
        api.drawActionCard(roomId, playerId).enqueue(object : Callback<Player> {
            override fun onResponse(call: Call<Player>, response: Response<Player>) {
                if (!response.isSuccessful) {
                    callback.onError("Не удалось взять карту. Код: ${response.code()}")
                    return
                }
                val playerFromServer = response.body()
                if (playerFromServer == null) {
                    callback.onError("Пустой ответ сервера при взятии карты")
                    return
                }

                // Конвертация Player -> PlayerData
                val newPlayerData = PlayerData(
                    playerId = playerFromServer.id,
                    login = playerFromServer.login,
                    roleCard = playerFromServer.role?.let { ContentCard(it.id, it.text) }
                        ?: ContentCard("", ""),
                    moodCard = playerFromServer.mood?.let { ContentCard(it.id, it.text) }
                        ?: ContentCard("", ""),
                    actionCards = playerFromServer.actions
                        ?.map { ContentCard(it.id, it.text) }
                        ?: emptyList()
                )

                // Обновляем репозиторий
                GameRepository.replacePlayerData(newPlayerData)

                // Callback в активити, если нужен
                callback.onSuccess(newPlayerData)
            }

            override fun onFailure(call: Call<Player>, t: Throwable) {
                callback.onError("Ошибка сети при взятии карты: ${t.message}")
            }
        })
    }


    fun changeRoleCard(
        roomId: String,
        playerId: String,
        callback: ChangeRoleCallback
    ) {
        val api = RetrofitClient.instance
        api.changeRoleCard(roomId, playerId).enqueue(object : Callback<Player> {
            override fun onResponse(
                call: Call<Player>,
                response: Response<Player>
            ) {
                if (!response.isSuccessful) {
                    callback.onError("Не удалось сменить роль. Код: ${response.code()}")
                    return
                }
                val body = response.body()
                if (body == null) {
                    callback.onError("Пустой ответ сервера при смене роли")
                    return
                }

                // Предположим, что server возвращает roleCards = ["новый текст роли"]
                val newRoleList = body.role?.text
                val newRole = newRoleList?.firstOrNull()
                if (newRole == null) {
                    callback.onError("В ответе нет новой роли")
                    return
                }

                // Обновляем локальный репозиторий по id
                GameRepository.updatePlayerRole(body.id, newRoleList.toString())
                Log.d("НОВАЯ КАРТА", newRoleList)
                callback.onSuccess(newRole.toString())
            }

            override fun onFailure(call: Call<Player>, t: Throwable) {
                callback.onError("Ошибка сети при смене роли: ${t.message}")
            }
        })
    }

    /**
     * Запрашивает у сервера новое настроение для указанного игрока.
     * После успешного ответа обновляет настроение в GameRepository.
     */
    fun changeMoodCard(
        roomId: String,
        playerId: String,
        callback: ChangeMoodCallback
    ) {
        val api = RetrofitClient.instance
        api.changeMoodCard(roomId, playerId).enqueue(object : Callback<Player> {
            override fun onResponse(
                call: Call<Player>,
                response: Response<Player>
            ) {
                if (!response.isSuccessful) {
                    callback.onError("Не удалось сменить настроение. Код: ${response.code()}")
                    return
                }
                val body = response.body()
                if (body == null) {
                    callback.onError("Пустой ответ сервера при смене настроения")
                    return
                }

                // Предположим, что server возвращает moodCards = ["новый текст настроения"]
                val newMoodList = body.mood?.text
                val newMood = newMoodList?.firstOrNull()
                if (newMood == null) {
                    callback.onError("В ответе нет нового настроения")
                    return
                }

                // Обновляем локальный репозиторий по login
                GameRepository.updatePlayerMood(body.id, newMoodList.toString())
                Log.d("НОВАЯ КАРТА", newMoodList)
                callback.onSuccess(newMood.toString())
            }

            override fun onFailure(call: Call<Player>, t: Throwable) {
                callback.onError("Ошибка сети при смене настроения: ${t.message}")
            }
        })
    }

    fun leaveRoom(
        roomId: String,
        playerId: String,
        callback: LeaveRoomCallback
    ) {
        val api = RetrofitClient.instance
        api.leaveRoom(roomId, playerId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError("Не удалось выйти из комнаты. Код: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                callback.onError("Ошибка сети при выходе из комнаты: ${t.message}")
            }
        })
    }
}