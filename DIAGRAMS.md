# Visual System Diagrams

## 1. Complete System Architecture

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                        CLIENT LAYER                          ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃                                                                ┃
┃  ┌─────────────┐      ┌─────────────┐      ┌─────────────┐   ┃
┃  │ LoginFrame  │─────▶│ LobbyFrame  │─────▶│  GameFrame  │   ┃
┃  └──────┬──────┘      └──────┬──────┘      └──────┬──────┘   ┃
┃         │                    │                     │           ┃
┃         │   ┌────────────────┴─────────────────────┘           ┃
┃         │   │                                                  ┃
┃         ▼   ▼                                                  ┃
┃  ┌──────────────────────┐                                     ┃
┃  │  ConnectionManager   │                                     ┃
┃  │  - Socket            │                                     ┃
┃  │  - Input/Output      │                                     ┃
┃  │  - Heartbeat         │                                     ┃
┃  └──────────┬───────────┘                                     ┃
┃             │                                                  ┃
┗━━━━━━━━━━━━━┿━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
              │
              │ TCP Socket (Port 8888)
              │ Text Protocol: COMMAND|PARAM1|PARAM2
              │
┏━━━━━━━━━━━━━┿━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃             │                 SERVER LAYER                     ┃
┣━━━━━━━━━━━━━┿━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃             ▼                                                  ┃
┃  ┌────────────────────┐                                       ┃
┃  │    ServerMain      │                                       ┃
┃  │  - ServerSocket    │                                       ┃
┃  │  - Accept Loop     │                                       ┃
┃  │  - Thread Pool     │                                       ┃
┃  └─────────┬──────────┘                                       ┃
┃            │                                                   ┃
┃            │ Creates                                           ┃
┃            ▼                                                   ┃
┃  ┌────────────────────┐                                       ┃
┃  │  ClientHandler     │◀───┐                                  ┃
┃  │  (Thread Pool)     │    │ Multiple Instances               ┃
┃  │  - Socket          │    │                                  ┃
┃  │  - Handle Commands │◀───┘                                  ┃
┃  └─────────┬──────────┘                                       ┃
┃            │                                                   ┃
┃            │ Uses                                              ┃
┃            ▼                                                   ┃
┃  ┌────────────────────────────────────────┐                   ┃
┃  │         ServerState                    │                   ┃
┃  │  ┌──────────────────────────────────┐  │                   ┃
┃  │  │ ConcurrentHashMap<String, CH>    │  │ Users            ┃
┃  │  │ ConcurrentHashMap<String, GR>    │  │ Games            ┃
┃  │  │ ConcurrentHashMap<String, String>│  │ Invites          ┃
┃  │  └──────────────────────────────────┘  │                   ┃
┃  └─────────┬──────────────────────────────┘                   ┃
┃            │                                                   ┃
┃            │ Manages                                           ┃
┃            ▼                                                   ┃
┃  ┌────────────────────┐                                       ┃
┃  │    GameRoom        │                                       ┃
┃  │  - player1, player2│                                       ┃
┃  │  - board[3][3]     │                                       ┃
┃  │  - currentTurn     │                                       ┃
┃  │  - checkWin()      │                                       ┃
┃  └────────────────────┘                                       ┃
┃                                                                ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

## 2. Complete Game Flow Sequence

```
Client A (Alice)            Server                Client B (Bob)
     │                        │                         │
     │ 1. TCP Connect         │                         │
     ├───────────────────────▶│                         │
     │                        │                         │
     │ 2. LOGIN|Alice         │                         │
     ├───────────────────────▶│                         │
     │                        │                         │
     │                   [Validate]                     │
     │                   [Register]                     │
     │                        │                         │
     │ 3. LOGIN_OK|Alice      │                         │
     │◀───────────────────────┤                         │
     │                        │                         │
     │ 4. USER_LIST|Alice     │                         │
     │◀───────────────────────┤                         │
     │                        │                         │
     │                        │    5. TCP Connect       │
     │                        │◀────────────────────────┤
     │                        │                         │
     │                        │    6. LOGIN|Bob         │
     │                        │◀────────────────────────┤
     │                        │                         │
     │                        │    [Validate & Register]│
     │                        │                         │
     │ 7. USER_LIST|Alice,Bob │    8. LOGIN_OK|Bob      │
     │◀───────────────────────┼────────────────────────▶│
     │                        │                         │
     │                        │    9. USER_LIST|...     │
     │                        ├────────────────────────▶│
     │                        │                         │
     │ 10. INVITE|Bob         │                         │
     ├───────────────────────▶│                         │
     │                        │                         │
     │                   [Create Invite]                │
     │                        │                         │
     │ 11. INVITE_SENT|Bob    │  12. INVITE_FROM|Alice  │
     │◀───────────────────────┼────────────────────────▶│
     │                        │                         │
     │                        │                         │
     │                        │  13. ACCEPT_INVITE      │
     │                        │◀────────────────────────┤
     │                        │                         │
     │                   [Create GameRoom]              │
     │                   [Assign X to Alice]            │
     │                   [Assign O to Bob]              │
     │                        │                         │
     │ 14. GAME_START         │  15. GAME_START         │
     │     you=X,opponent=Bob │      you=O,opponent=Alice
     │◀───────────────────────┼────────────────────────▶│
     │                        │                         │
     │ 16. YOUR_TURN          │                         │
     │◀───────────────────────┤                         │
     │                        │                         │
     │ 17. BOARD_UPDATE|...   │  18. BOARD_UPDATE|...   │
     │◀───────────────────────┼────────────────────────▶│
     │                        │                         │
     │ 19. MOVE|0|0           │                         │
     ├───────────────────────▶│                         │
     │                        │                         │
     │                   [Validate Move]                │
     │                   [Update Board]                 │
     │                   [Check Win]                    │
     │                   [Switch Turn]                  │
     │                        │                         │
     │ 20. BOARD_UPDATE|X...  │  21. BOARD_UPDATE|X...  │
     │◀───────────────────────┼────────────────────────▶│
     │                        │                         │
     │                        │  22. YOUR_TURN          │
     │                        ├────────────────────────▶│
     │                        │                         │
     │                        │  23. MOVE|1|1           │
     │                        │◀────────────────────────┤
     │                        │                         │
     │                   [Process Move...]              │
     │                        │                         │
     │ 24. BOARD_UPDATE|X.O.  │  25. BOARD_UPDATE|X.O.  │
     │◀───────────────────────┼────────────────────────▶│
     │                        │                         │
     │ 26. YOUR_TURN          │                         │
     │◀───────────────────────┤                         │
     │                        │                         │
     │        [... more moves ...]                      │
     │                        │                         │
     │ 27. MOVE|0|2 (wins!)   │                         │
     ├───────────────────────▶│                         │
     │                        │                         │
     │                   [Check Win: Alice wins!]       │
     │                   [End Game]                     │
     │                        │                         │
     │ 28. BOARD_UPDATE|XXO   │  29. BOARD_UPDATE|XXO   │
     │◀───────────────────────┼────────────────────────▶│
     │                        │                         │
     │ 30. GAME_RESULT|WIN    │  31. GAME_RESULT|LOSE   │
     │◀───────────────────────┼────────────────────────▶│
     │                        │                         │
     │ [Return to Lobby]      │    [Return to Lobby]    │
     │                        │                         │
```

## 3. Thread Model Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     SERVER PROCESS                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Main Thread                                                │
│  ┌───────────────────────────────────────┐                 │
│  │  ServerMain                           │                 │
│  │  ┌─────────────────────────────────┐  │                 │
│  │  │ while(true) {                   │  │                 │
│  │  │   socket = accept()             │  │                 │
│  │  │   threadPool.execute(           │  │                 │
│  │  │     new ClientHandler(socket)   │  │                 │
│  │  │   )                              │  │                 │
│  │  │ }                                │  │                 │
│  │  └─────────────────────────────────┘  │                 │
│  └───────────────┬───────────────────────┘                 │
│                  │                                          │
│                  │ spawns                                   │
│                  ▼                                          │
│  ┌────────────────────────────────────────────────┐        │
│  │  Thread Pool (ExecutorService)                 │        │
│  │  ┌──────────────┐  ┌──────────────┐           │        │
│  │  │ClientHandler │  │ClientHandler │  ...      │        │
│  │  │  (Thread 1)  │  │  (Thread 2)  │           │        │
│  │  │              │  │              │           │        │
│  │  │ run() {      │  │ run() {      │           │        │
│  │  │   readLine() │  │   readLine() │           │        │
│  │  │   handle()   │  │   handle()   │           │        │
│  │  │ }            │  │ }            │           │        │
│  │  └──────────────┘  └──────────────┘           │        │
│  └────────────────────────────────────────────────┘        │
│                                                             │
│  Health Monitor Thread (Daemon)                             │
│  ┌─────────────────────────────────────────┐               │
│  │ while(true) {                           │               │
│  │   sleep(30s)                            │               │
│  │   cleanupInactiveClients()              │               │
│  │ }                                        │               │
│  └─────────────────────────────────────────┘               │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     CLIENT PROCESS                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Event Dispatch Thread (EDT) - Swing UI                    │
│  ┌─────────────────────────────────────────┐               │
│  │  LoginFrame / LobbyFrame / GameFrame    │               │
│  │  - Button clicks                        │               │
│  │  - Text input                           │               │
│  │  - UI updates                           │               │
│  └─────────────────────────────────────────┘               │
│                                                             │
│  Message Listener Thread                                    │
│  ┌─────────────────────────────────────────┐               │
│  │  while(connected) {                     │               │
│  │    msg = readLine()                     │               │
│  │    SwingUtilities.invokeLater(() -> {   │               │
│  │      processMessage(msg)                │               │
│  │    })                                   │               │
│  │  }                                       │               │
│  └─────────────────────────────────────────┘               │
│                                                             │
│  Heartbeat Thread (Scheduled)                               │
│  ┌─────────────────────────────────────────┐               │
│  │  every 15s {                            │               │
│  │    sendMessage("PING")                  │               │
│  │    check timeout                        │               │
│  │  }                                       │               │
│  └─────────────────────────────────────────┘               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 4. State Machine - Game States

```
                    ┌─────────┐
                    │ WAITING │
                    └────┬────┘
                         │
                         │ startGame()
                         ▼
              ┌──────────────────┐
              │   IN_PROGRESS    │◀────────┐
              └────┬────┬────┬───┘         │
                   │    │    │             │
         makeMove()│    │    │handleSurr() │
                   │    │    │             │
         ┌─────────┘    │    └────────┐    │
         │              │             │    │
         │     checkWin()  checkDraw()│    │
         │              │             │    │
         ▼              ▼             ▼    │
    ┌────────┐    ┌─────────┐    ┌──────────┐
    │  WIN   │    │  DRAW   │    │SURRENDER │
    └────┬───┘    └────┬────┘    └────┬─────┘
         │             │              │
         │             │              │
         └─────────────┴──────────────┘
                       │
                       ▼
                 ┌──────────┐
                 │ FINISHED │
                 └──────────┘
```

## 5. Data Flow - Move Processing

```
┌─────────────────────────────────────────────────────────────┐
│                     MOVE PROCESSING FLOW                    │
└─────────────────────────────────────────────────────────────┘

Client                    Server                    Opponent
  │                         │                          │
  │ 1. User clicks cell     │                          │
  │    (row=1, col=2)       │                          │
  │                         │                          │
  │ 2. MOVE|gameId|1|2      │                          │
  ├────────────────────────▶│                          │
  │                         │                          │
  │                    3. Validate                     │
  │                       ┌──┴──┐                      │
  │                       │ OK? │                      │
  │                       └──┬──┘                      │
  │                          │                         │
  │                    NO ◀──┴──▶ YES                  │
  │                     │          │                   │
  │                     │          │ 4. Update board   │
  │                     │          │    board[1][2] = X│
  │                     │          │                   │
  │ 5. ERROR|reason     │          │ 5. Check win      │
  │◀────────────────────┤          │    ┌───┴───┐     │
  │                     │          │    │ Win?  │     │
  │                                │    └───┬───┘     │
  │                          NO ◀──┴──▶ YES │         │
  │                           │          │   │         │
  │                           │          │   │ 6. End  │
  │                           │          │   │    Game │
  │ 6. BOARD_UPDATE           │ 7. Switch  │         │
  │    |gameId|X....O.X       │    turn    │         │
  │◀──────────────────────────┤          │         │
  │                           │          │         │
  │                           │          │ 7. GAME_RESULT
  │                           │          ├───────────▶│
  │                           │          │         │
  │                           │ 8. YOUR_TURN      │
  │                           ├───────────────────▶│
  │                           │                   │
```

## 6. Error Handling Flow

```
┌─────────────────────────────────────────────────────────────┐
│                  ERROR HANDLING HIERARCHY                   │
└─────────────────────────────────────────────────────────────┘

┌──────────────────┐
│ Network Errors   │
└────────┬─────────┘
         │
         ├─▶ SocketTimeoutException
         │   └─▶ Log + Retry / Disconnect
         │
         ├─▶ ConnectException
         │   └─▶ Show error + Return to login
         │
         ├─▶ IOException (Read/Write)
         │   └─▶ Detect via heartbeat
         │       └─▶ Notify user
         │           └─▶ Clean up resources
         │
         └─▶ Connection Lost
             └─▶ Server: Remove user + Notify others
             └─▶ Client: Show dialog + Exit/Retry

┌──────────────────┐
│ Protocol Errors  │
└────────┬─────────┘
         │
         ├─▶ Malformed Message
         │   └─▶ ArrayIndexOutOfBoundsException
         │       └─▶ Send ERROR response
         │           └─▶ Log warning
         │
         ├─▶ Invalid Parameters
         │   └─▶ NumberFormatException
         │       └─▶ Send descriptive error
         │
         └─▶ Unknown Command
             └─▶ Log + Send ERROR

┌──────────────────┐
│  Game Errors     │
└────────┬─────────┘
         │
         ├─▶ Invalid Move
         │   └─▶ Not your turn
         │   └─▶ Cell occupied
         │   └─▶ Out of bounds
         │       └─▶ Send ERROR + Continue game
         │
         ├─▶ Player Disconnect
         │   └─▶ Opponent wins
         │       └─▶ Clean up game
         │
         └─▶ Game State Corruption
             └─▶ Log error
                 └─▶ End game safely
                     └─▶ Notify both players
```

---

**Legend:**
- `│` = Connection/Flow
- `┌─┐` = Component/State
- `▶` = Direction of flow
- `◀──┴──▶` = Decision point
- `...` = Continuation/More items