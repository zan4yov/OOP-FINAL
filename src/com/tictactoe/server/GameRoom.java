// GameRoom.java
package com.tictactoe.server;

import java.util.Arrays;

public class GameRoom {

    private final String gameId;
    private final String player1; // inviter = X
    private final String player2; // invitee = O

    private final ServerState serverState;

    private char[] board; // 9 cells
    private String currentTurn; // username whose turn it is
    private GameStatus status;
    private String winner;

    public enum GameStatus {
        WAITING,
        IN_PROGRESS,
        FINISHED
    }

    public GameRoom(String gameId, String player1, String player2, ServerState serverState) {
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.serverState = serverState;

        this.board = new char[]{'-','-','-','-','-','-','-','-','-'};
        this.status = GameStatus.WAITING;
        this.currentTurn = player1; // X always starts
    }

    // ----------------------
    // GAME START
    // ----------------------
    public void startGame() {
        this.status = GameStatus.IN_PROGRESS;
        this.currentTurn = player1; // X turn first

        ClientHandler h1 = serverState.getUser(player1);
        ClientHandler h2 = serverState.getUser(player2);

        // Notify P1 = X
        if (h1 != null) {
            h1.sendMessage("GAME_START|" + gameId + "|you=X|opponent=" + player2);
            // First turn:
            h1.sendMessage("YOUR_TURN|" + gameId);
        }

        // Notify P2 = O
        if (h2 != null) {
            h2.sendMessage("GAME_START|" + gameId + "|you=O|opponent=" + player1);
        }

        sendBoardUpdate();
    }

    // ----------------------
    // MOVE PROCESSING
    // ----------------------
    public synchronized void processMove(String username, int cellIndex) {

        if (status != GameStatus.IN_PROGRESS) return;
        if (!username.equals(currentTurn)) return;
        if (cellIndex < 0 || cellIndex > 8) return;
        if (board[cellIndex] != '-') return;

        char symbol = username.equals(player1) ? 'X' : 'O';
        board[cellIndex] = symbol;

        sendBoardUpdate();

        // Check win/draw
        if (checkWinner(symbol)) {
            status = GameStatus.FINISHED;
            winner = username;
            broadcast("GAME_RESULT|" + gameId + "|WIN|" + username);

            String loser = username.equals(player1) ? player2 : player1;
            broadcast("GAME_RESULT|" + gameId + "|LOSE|" + loser);
            return;
        }

        if (isBoardFull()) {
            status = GameStatus.FINISHED;
            broadcast("GAME_RESULT|" + gameId + "|DRAW|NONE");
            return;
        }

        switchTurn();
        notifyTurn();
    }

    // ----------------------
    // TURN HANDLING
    // ----------------------
    private void switchTurn() {
        currentTurn = currentTurn.equals(player1) ? player2 : player1;
    }

    private void notifyTurn() {
        ClientHandler h = serverState.getUser(currentTurn);
        if (h != null) {
            h.sendMessage("YOUR_TURN|" + gameId);
        }
    }

    // ----------------------
    // BOARD UPDATE
    // ----------------------
    private void sendBoardUpdate() {
        String boardString = new String(board);
        broadcast("BOARD_UPDATE|" + gameId + "|" + boardString);
    }

    // ----------------------
    // BROADCAST TO BOTH PLAYERS
    // ----------------------
    public void broadcast(String msg) {
        ClientHandler h1 = serverState.getUser(player1);
        ClientHandler h2 = serverState.getUser(player2);

        if (h1 != null) h1.sendMessage(msg);
        if (h2 != null) h2.sendMessage(msg);
    }

    // ----------------------
    // WIN CHECK
    // ----------------------
    private boolean checkWinner(char symbol) {
        int[][] lines = {
            {0,1,2},{3,4,5},{6,7,8}, // rows
            {0,3,4},{1,4,7},{2,5,8}, // cols
            {0,4,8},{2,4,6}          // diagonals
        };

        for (int[] line: lines) {
            if (board[line[0]] == symbol &&
                board[line[1]] == symbol &&
                board[line[2]] == symbol) {
                return true;
            }
        }
        return false;
    }

    private boolean isBoardFull() {
        for (char c : board) {
            if (c == '-') return false;
        }
        return true;
    }

    // ----------------------
    // GETTERS
    // ----------------------
    public String getPlayer1() { return player1; }
    public String getPlayer2() { return player2; }
    public String getGameId() { return gameId; }
    public GameStatus getStatus() { return status; }
    public String getWinner() { return winner; }
}
