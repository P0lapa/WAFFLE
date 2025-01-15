# Описание WEB API

## HTTP-запросы (запрос - ответ)

{id}  в запросе - параметр
### Проверка подключения

GET /ping

RESPONSE:
pong
### Комната

#### Добавление

POST /rooms
``` json
{
    "creatorLogin": "Petya",
    "cardsPerPlayer": 1,
    "situationCards": [
        "Situation card 1",
        "Situation card 2"
    ],
    "roleCards": [
        "Role Card 1",
        "Role Card 2",
        "Role Card 3"
    ],
    "moodCards": [
        "Mood Card 1",
        "Mood Card 2",
        "Mood Card 3"
    ],
    "actionCards": [
        "Action Card 1",
        "Action Card 2",
        "Action Card 3"
    ]
}
```

RESPONSE:
roomID

#### Получение

GET /rooms/roomID

RESPONSE:
Room

#### Удаление

DELETE /rooms/roomID

### Игрок

#### Добавление

POST /rooms/{roomId}/players
`Login`

RESPONSE:
Player
#### Получение

GET /rooms/{roomId}/players/{playerId}

RESPONSE:
Player
#### Удаление

DELETE /rooms/{roomId}/players/{playerId}
### Смена карты роли/настроения


## WebSocket (запрос - оповещение)

> [!INFO] Используется SockJS и STOMP поверх WebSocket

### Пример с подключением и стартом игры

``` HTML
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WebSocket Test</title>
</head>
<body>
    <h1>WebSocket STOMP Test</h1>
    <div>
        <input id="roomIdInput" type="text">
    </div>
    <div>
        <button id="connect">Connect</button>
        <button id="disconnect" disabled>Disconnect</button>
    </div>
    <div>
        <button id="sendMessage">Start game</button>
    </div>
    <div>
        <h2>Received Messages</h2>
        <ul id="messages"></ul>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>
    <script>
        let stompClient = null;

        function setConnected(connected) {
            document.getElementById("connect").disabled = connected;
            document.getElementById("disconnect").disabled = !connected;
            document.getElementById("sendMessage").disabled = !connected;
        }

        function connect() {
            const socket = new SockJS('http://localhost:8080/ws');
            stompClient = Stomp.over(socket);
            stompClient.connect({}, () => {
                setConnected(true);
                console.log("Connected to WebSocket");

                // Подписываемся на топик
                const roomId = document.getElementById("roomIdInput").value;
                stompClient.subscribe('/topic/' + roomId, (message) => {
                    const li = document.createElement("li");
                    li.textContent = "Received: " + message.body;
                    document.getElementById("messages").appendChild(li);
                });
            });
        }

        function disconnect() {
            if (stompClient !== null) {
                stompClient.disconnect(() => {
                    console.log("Disconnected");
                    setConnected(false);
                });
            }
        }

        function sendMessage() {
            const roomId = document.getElementById("roomIdInput").value;
            stompClient.send('/room/' + roomId + '/start', {}, '{"roomId": '+ roomId +'}');
        }

        document.getElementById("connect").addEventListener("click", connect);
        document.getElementById("disconnect").addEventListener("click", disconnect);
        document.getElementById("sendMessage").addEventListener("click", sendMessage);
    </script>
</body>
</html>

```
### Подключение
/ws
SUBSCRIBE /topic/{roomID}
### Старт игры

SEND /room/{roomId}/start
``` JSON
{
    "roomId": roomID
}
```


### Завершение игры

Выход игрока!
Смена карты ситуации!
Сброс активной карты!
Взятие активной карты+
Перемешать колоду!





