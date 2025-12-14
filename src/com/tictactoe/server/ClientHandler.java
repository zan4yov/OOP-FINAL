package com.tictactoe.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ServerState serverState;

    private PrintWriter out;
    private BufferedReader in;

    private String username;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, ServerState serverState) {
        this.socket = socket;
        this.serverState = serverState;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while (running && (line = in.readLine()) != null) {
                System.out.println("[SERVER] Received: " + line);
                handleMessage(line);
            }
        } catch (IOException e) {
            System.out.println("[SERVER] Connection error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleMessage(String msg) {
        String[] parts = msg.split("\\|");
        String cmd = parts[0];

        switch (cmd) {
            case "LOGIN":
                if (parts.length >= 2) {
                    handleLogin(parts[1]);
                }
                break;

            case "CHAT_GLOBAL":
                if (username != null && parts.length >= 2) {
                    String text = parts[1];
                    serverState.broadcast("CHAT_GLOBAL_FROM|" + username + "|" + text);
                }
                break;

            case "INVITE":
                if (username != null && parts.length >= 2) {
                    handleInvite(parts[1]);
                }
                break;

            case "ACCEPT_INVITE":
                if (username != null && parts.length >= 2) {
                    handleInviteAccepted(parts[1]);
                }
                break;

            case "DECLINE_INVITE":
                if (username != null && parts.length >= 2) {
                    handleInviteDeclined(parts[1]);
                }
                break;

            case "GAME_MOVE":
                if (username != null && parts.length >= 3) {
                    handleGameMove(parts);
                }
                break;

            case "GAME_CHAT":
                if (username != null && parts.length >= 3) {
                    handleGameChat(parts);
                }
                break;

            case "GAME_SURRENDER":
                if (username != null) {
                    handleSurrender();
                }
                break;

            case "PING":
                // heartbeat dari client
                sendMessage("PONG");
                break;

            case "REQ_USER_LIST":
                serverState.broadcastUserList();
                break;

            case "QUIT":
                running = false;
                disconnect();
                break;

            default:
                // boleh diabaikan atau di-log
                // sendMessage("INVALID_COMMAND");
                break;
        }
    }

    // ================= LOGIN =================

    private void handleLogin(String name) {
        if (serverState.isUsernameTaken(name)) {
            sendMessage("LOGIN_FAIL|Username already used.");
            return;
        }

        this.username = name;
        serverState.addUser(name, this);

        // Beri tahu client yang login
        sendMessage("LOGIN_OK|" + name);

        // Sebarkan user list ke SEMUA client (ini yang penting)
        serverState.broadcastUserList();

        System.out.println("[SERVER] User logged in: " + name);
    }

    // ================= INVITE / GAME START =================

    private void handleInvite(String target) {
        ClientHandler targetHandler = serverState.getUser(target);
        if (targetHandler != null) {
            targetHandler.sendMessage("INVITE_FROM|" + username);
        }
    }

    private void handleInviteAccepted(String inviterName) {
        ClientHandler inviter = serverState.getUser(inviterName);
        ClientHandler invitee = serverState.getUser(username);

        if (inviter == null || invitee == null)
            return;

        // Buat game baru
        GameRoom game = serverState.createGame(inviterName, username);

        inviter.sendMessage("INVITE_ACCEPTED|" + username);
        invitee.sendMessage("INVITE_ACCEPTED|" + inviterName);

        // Mulai game (akan mengirim GAME_START + YOUR_TURN)
        game.startGame();
    }

    private void handleInviteDeclined(String inviterName) {
        ClientHandler inviter = serverState.getUser(inviterName);
        if (inviter != null) {
            inviter.sendMessage("INVITE_DECLINED|" + username);
        }
    }

    // ================= GAME PLAY =================

    private void handleGameMove(String[] parts) {
        String gameId = parts[1];
        int cellIndex = Integer.parseInt(parts[2]);

        GameRoom game = serverState.getGame(gameId);
        if (game != null) {
            game.processMove(username, cellIndex);
        }
    }

    private void handleGameChat(String[] parts) {
        String gameId = parts[1];
        String text = parts[2];

        GameRoom game = serverState.getGame(gameId);
        if (game != null) {
            game.broadcast("CHAT_GAME_FROM|" + username + "|" + text);
        }
    }

    private void handleSurrender() {
        GameRoom game = serverState.findGameByPlayer(username);
        if (game == null)
            return;

        String p1 = game.getPlayer1();
        String p2 = game.getPlayer2();
        String winner = username.equals(p1) ? p2 : p1;

        game.broadcast("GAME_RESULT|" + game.getGameId() + "|WIN|" + winner);
        game.broadcast("GAME_RESULT|" + game.getGameId() + "|LOSE|" + username);

        // optional: hapus game dari server
        serverState.removeGame(game.getGameId());
    }

    // ================= DISCONNECT =================

    private void disconnect() {
        try {
            running = false;

            if (username != null) {
                System.out.println("[SERVER] User disconnected: " + username);
                serverState.removeUser(username);
                serverState.broadcastUserList();
            }

            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null && !socket.isClosed())
                socket.close();

        } catch (IOException ignored) {
        }
    }

    // ================= UTIL =================

    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    public String getUsername() {
        return username;
    }
}
