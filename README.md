# Multiplayer Tic-Tac-Toe Game

## 🎮 Overview
Java-based multiplayer Tic-Tac-Toe dengan client-server architecture, GUI berbasis Swing, TCP socket communication, dan real-time gameplay.

## ✨ Features
- ✅ **Client-Server Architecture** menggunakan TCP sockets
- ✅ **Multi-threaded Server** mendukung multiple concurrent clients
- ✅ **GUI Interface** dengan Java Swing
- ✅ **Infinity Tic-Tac-Toe** - Max 3 moves per player (oldest move disappears)
- ✅ **Lobby System** with user list (excludes self) and invitation system
- ✅ **Chat System** - Global chat and in-game chat
- ✅ **Connection Management** with heartbeat and auto-reconnect detection
- ✅ **Secure Game State** with server-side validation

## 📋 Requirements
- Java Development Kit (JDK) 8 or higher
- OS: Windows, Linux, or macOS
- Network: LAN or Internet connection
- Port 8888 must be available

## 🚀 Quick Start

### 1. Clone/Download Project
```bash
git clone <repository-url>
cd tictactoe
```

### 2. Build Project

**Linux/Mac:**
```bash
chmod +x build.sh
./build.sh
```

**Windows:**
```cmd
build.bat
```

### 3. Run Server

**Linux/Mac:**
```bash
./run-server.sh
```

**Windows:**
```cmd
run-server.bat
```

### 4. Run Client (new terminal/window)

**Linux/Mac:**
```bash
./run-client.sh
```

**Windows:**
```cmd
run-client.bat
```

### 5. Connect and Play!
1. Enter username (3-15 alphanumeric characters)
2. Enter server address (default: localhost)
3. Enter port (default: 8888)
4. Click "Connect"
5. Invite other players from lobby (you won't see yourself in the list)
6. Enjoy the game!

## 📁 Project Structure
```
tictactoe/
├── src/
│   └── com/
│       └── tictactoe/
│           ├── server/
│           │   ├── ServerMain.java          # Main server class
│           │   ├── ServerState.java         # State management
│           │   ├── ClientHandler.java       # Handle client connections
│           │   └── GameRoom.java            # Game logic
│           └── client/
│               ├── ClientMain.java          # Main client class
│               ├── ConnectionManager.java   # Network management
│               ├── LoginFrame.java          # Login GUI
│               ├── LobbyFrame.java          # Lobby GUI
│               └── GameFrame.java           # Game GUI
├── bin/                                     # Compiled classes
├── test/                                    # Unit tests
├── docs/                                    # Documentation
├── build.sh / build.bat                     # Build scripts
├── run-server.sh / run-server.bat           # Server run scripts
├── run-client.sh / run-client.bat           # Client run scripts
└── README.md                                # This file
```

## 🎯 How to Play

### Login
1. Launch client application
2. Enter a unique username
3. Specify server address (localhost for same machine)
4. Click Connect

### Lobby
- View all online users (except yourself)
- Send messages in global chat
- Select a user and click "Invite to Play"
- Wait for invitation acceptance or invite others

### Game Rules (Infinity Tic-Tac-Toe)
- **X always goes first** (assigned to inviter)
- **Max 3 Moves**: Each player can have at most **3 symbols** on the board.
- **Infinity Logic**: When you place your **4th mark**, your **1st mark (oldest)** automatically disappears!
- **Strategy**: You must plan ahead, as your old moves will vanish.
- **Win**: Get 3 in a row (horizontal, vertical, or diagonal).
- **Draw**: Not applicable in Infinity mode (game continues until someone wins or surrenders).

### In-Game
- Click empty cell to place your symbol
- Wait for your turn (indicated by status label)
- Use in-game chat to talk with opponent
- Click "Surrender" to give up
- Automatically return to lobby after game ends

## 🔧 Configuration

### Change Server Port
Edit `ServerMain.java`:
```java
private static final int PORT = 8888; // Change this
```

### Change Maximum Clients
Edit `ServerMain.java`:
```java
private static final int MAX_CLIENTS = 50; // Change this
```

### Change Default Connection Settings
Edit `LoginFrame.java`:
```java
private static final String DEFAULT_SERVER = "localhost";
private static final String DEFAULT_PORT = "8888";
```

## 🧪 Testing

### Run Unit Tests
```bash
# Compile tests
javac -cp .:junit-platform-console-standalone.jar -d bin test/**/*.java

# Run tests
java -jar junit-platform-console-standalone.jar --class-path bin --scan-classpath
```

### Manual Testing Scenarios
1. **Single Player**: Login and explore lobby
2. **Two Players**: Login with 2 clients, invite, and play
3. **Infinity Check**: Place 4th move and verify 1st move disappears
4. **Disconnect**: Close client during game, verify opponent notification
5. **Server Restart**: Restart server, verify client error handling

## 📊 Protocol Specification

### Message Format
```
COMMAND|PARAM1|PARAM2|...
```

### Key Commands

**Client → Server:**
- `LOGIN|username` - Login request
- `REQ_USER_LIST` - Request fresh user list
- `INVITE|target` - Invite player to game
- `MOVE|gameId|row|col` - Make move
- `CHAT_GLOBAL|message` - Send global chat
- `SURRENDER|gameId` - Surrender game

**Server → Client:**
- `LOGIN_OK|username` - Login successful
- `USER_LIST|user1,user2,...` - Online users
- `GAME_START|gameId|you=X|opponent=name` - Game starting
- `BOARD_UPDATE|gameId|state` - Board state (9 chars: X, O, .)
- `GAME_RESULT|gameId|WINNER|username` - Game won by username
- `GAME_RESULT|gameId|DRAW|NONE` - Game drawn (rare in infinity mode)
- `CHAT_GLOBAL_FROM|sender|message` - Chat message

See full protocol documentation in `docs/PROTOCOL.md`

## 🐛 Troubleshooting

### Connection Issues
- **"Cannot connect to server"**: Ensure server is running first
- **"Connection lost"**: Check network connection, server might have crashed
- **"Address already in use"**: Port 8888 is occupied, change port or kill process

### Game Issues
- **"Cannot make move"**: Wait for your turn or check if cell is empty
- **"Invite failed"**: User might be offline or already in a game

### Performance Issues
- **Lag**: Check network latency, reduce number of clients
- **High CPU**: Monitor thread pool, check for infinite loops

See full troubleshooting guide in `docs/TROUBLESHOOTING.md`

## 📚 Documentation

- **[Complete Documentation](docs/DOCUMENTATION.md)** - Full technical documentation
- **[Protocol Specification](docs/PROTOCOL.md)** - Communication protocol details
- **[Architecture Guide](docs/ARCHITECTURE.md)** - System design and architecture
- **[Test Specification](docs/TESTING.md)** - Testing strategy and test cases
- **[User Manual](docs/USER_MANUAL.md)** - End-user guide

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📝 License
This is an educational project. Free to use for learning purposes.

## 👥 Authors
- Created as final project for Java Network Programming course
- Demonstrates client-server architecture, multithreading, and GUI programming

## 🙏 Acknowledgments
- Java Swing documentation
- Java Socket Programming tutorials
- Tic-Tac-Toe game logic references

## 📧 Contact & Support

For bugs, questions, or suggestions:
- Open an issue on GitHuh
---

**Happy Gaming! 🎮**