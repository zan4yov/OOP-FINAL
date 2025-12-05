package com.tictactoe.server;

import java.util.HashMap;
import java.util.Map;

public class ServerState {

    private final Map<String, ClientHandler> users = new HashMap<>();
    private final Map<String, GameRoom> games = new HashMap<>();

    // ========== USER MANAGEMENT ==========

    public synchronized void addUser(String username, ClientHandler handler) {
        users.put(username, handler);
    }

    public synchronized void removeUser(String username) {
        users.remove(username);
    }

    public synchronized boolean isUsernameTaken(String username) {
        return users.containsKey(username);
    }

    public synchronized ClientHandler getUser(String username) {
        return users.get(username);
    }

    public synchronized int getUserCount() {
        return users.size();
    }

    // ========== BROADCAST ==========

    public synchronized void broadcast(String msg) {
        for (ClientHandler handler : users.values()) {
            handler.sendMessage(msg);
        }
    }

    public synchronized void broadcastUserList() {
        String list = String.join(",", users.keySet());
        broadcast("USER_LIST|" + list);
        System.out.println("[SERVER] USER_LIST broadcast: " + list);
    }

    // ========== GAME MANAGEMENT ==========

    public synchronized GameRoom createGame(String player1, String player2) {
        String gameId = "GAME_" + System.currentTimeMillis();
        GameRoom game = new GameRoom(gameId, player1, player2, this);
        games.put(gameId, game);
        System.out.println("[SERVER] Game created: " + gameId + " (" + player1 + " vs " + player2 + ")");
        return game;
    }

    public synchronized GameRoom getGame(String gameId) {
        return games.get(gameId);
    }

    public synchronized void removeGame(String gameId) {
        games.remove(gameId);
    }

    public synchronized GameRoom findGameByPlayer(String username) {
        for (GameRoom game : games.values()) {
            if (username.equals(game.getPlayer1()) || username.equals(game.getPlayer2())) {
                return game;
            }
        }
        return null;
    }
}
