// WebSocketManager.kt
package com.example.waffle_front.OthersActivity

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED
import ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR
import ua.naiksoftware.stomp.dto.LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT
import ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED

object WebSocketManager {
    private const val SERVER_URL = "http://193.124.15.94:8000/ws/websocket"
    private lateinit var stompClient: StompClient
    private val disposables = CompositeDisposable()
    private var currentRoomId: String = ""

    fun isConnected() = ::stompClient.isInitialized && stompClient.isConnected

    fun connect(roomId: String, onConnected: () -> Unit = {}, onError: (String) -> Unit = {}) {
        currentRoomId = roomId
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SERVER_URL)

        disposables.add(
            stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { life ->
                    when (life.type) {
                        OPENED -> {
                            Log.d("STOMP","✅ connected")
                            subscribeToErrors(onError)
                            subscribeToTopic()
                            onConnected()
                        }
                        ERROR -> {
                            Log.e("STOMP","❌ conn error", life.exception)
                        }
                        CLOSED -> {
                            Log.d("STOMP","🔒 closed")
                        }

                        FAILED_SERVER_HEARTBEAT -> TODO()
                    }
                }
        )
        stompClient.connect()
    }

    private fun subscribeToTopic() {
        disposables.add(
            stompClient.topic("/topic/$currentRoomId")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    GameRepository.handleWebSocketEvent(msg.payload)
                }, { err ->
                    Log.e("STOMP","sub err", err)
                })
        )
    }

    private fun subscribeToErrors(onError: (String) -> Unit) {
        disposables.add(
            stompClient.topic("/user/queue/errors")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    Log.e("STOMP", "Received error: ${msg.payload}")
                    onError(msg.payload)
                }, { err ->
                    Log.e("STOMP", "Error subscription error", err)
                    onError(err.message ?: "Error subscription failed")
                })
        )
    }

//    private fun subscribeToRoomTopic() {
//        disposables.add(
//            stompClient.topic("/topic/$currentRoomId")
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ msg ->
//                    Log.d("STOMP", "recv: ${msg.payload}")
//                    GameRepository.handleWebSocketEvent(msg.payload)
//                }, { err ->
//                    Log.e("STOMP", "sub err", err)
//                })
//        )
//    }

    fun sendStartMessage(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (!isConnected()) {
            onError("not connected")
            return
        }
        val json = Json.encodeToString(StartMessage(currentRoomId))
        disposables.add(
            stompClient.send("/room/$currentRoomId/start", json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("STOMP", "start sent")
                    onSuccess()
                }, { err ->
                    onError("send failed: ${err.message}")
                })
        )
    }

    fun disconnect() {
        disposables.clear()
        if (::stompClient.isInitialized) stompClient.disconnect()
        Log.d("STOMP", "disconnected")
    }

    @Serializable
    private data class StartMessage(val roomId: String)
}
