package com.tictactoe.client;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame implements ConnectionManager.MessageListener {

    private final ConnectionManager cm;
    private final LobbyFrame lobby;
    private final String gameId;
    private final String youSymbol;
    private final String opponentName;

    private JLabel turnLabel;
    private JButton[] cells = new JButton[9];

    private JTextArea chatArea = new JTextArea();
    private JTextField chatInput = new JTextField();

    public GameFrame(ConnectionManager cm, LobbyFrame lobby,
                     String gameId, String youSymbol, String opponentName) {
        this.cm = cm;
        this.lobby = lobby;
        this.gameId = gameId;
        this.youSymbol = youSymbol;
        this.opponentName = opponentName;

        initUI();

        // jadikan GameFrame sebagai listener aktif
        cm.setMessageListener(this);
    }

    private void initUI() {
        setTitle("Game vs " + opponentName);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // TOP: status
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        turnLabel = new JLabel("Waiting for your turn...", SwingConstants.CENTER);
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel opponentLabel = new JLabel("Opponent: " + opponentName, SwingConstants.CENTER);
        opponentLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        topPanel.add(turnLabel);
        topPanel.add(opponentLabel);

        add(topPanel, BorderLayout.NORTH);

        // CENTER: board
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        for (int i = 0; i < 9; i++) {
            JButton btn = new JButton("");
            btn.setFont(new Font("SansSerif", Font.BOLD, 48));
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFocusable(false);

            int idx = i;
            btn.addActionListener(e -> handleCellClick(idx));
            btn.setEnabled(false);

            cells[i] = btn;
            boardPanel.add(btn);
        }

        add(boardPanel, BorderLayout.CENTER);

        // RIGHT: chat in-game
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        chatInput.addActionListener(e -> sendGameChat());

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JLabel("Game Chat", SwingConstants.CENTER), BorderLayout.NORTH);
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);

        chatPanel.setPreferredSize(new Dimension(260, 0));

        add(chatPanel, BorderLayout.EAST);

        setVisible(true);
    }

    private void handleCellClick(int idx) {
    cm.sendMessage("GAME_MOVE|" + gameId + "|" + idx);

    // Setelah mengambil langkah → harus menunggu
    turnLabel.setText("Wait your turn...");
    turnLabel.setForeground(Color.RED.darker());

    enableBoard(false);
}

    private void enableBoard(boolean enable) {
        for (JButton btn : cells) {
            if (btn.getText().isEmpty())
                btn.setEnabled(enable);
        }
    }

    private void sendGameChat() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) return;

        cm.sendMessage("GAME_CHAT|" + gameId + "|" + msg);
        chatInput.setText("");
    }

    @Override
    public void onMessageReceived(String message) {
        String[] parts = message.split("\\|");
        String cmd = parts[0];

        switch (cmd) {

            case "YOUR_TURN":
                // (opsional) bisa cek gameId = parts[1] jika server kirim
                turnLabel.setText("Your turn (" + youSymbol + ")");
                turnLabel.setForeground(Color.GREEN.darker());
                enableBoard(true);
                break;

            case "BOARD_UPDATE":
                // parts[1] = gameId, parts[2] = board string
                if (parts.length >= 3) {
                    updateBoard(parts[2]);
                }
                break;

            case "GAME_RESULT":
                handleGameResult(parts);
                break;

            case "CHAT_GAME_FROM":
                // format: CHAT_GAME_FROM|sender|msg
                String sender = parts[1];
                String msg = parts[2];
                chatArea.append(sender + ": " + msg + "\n");
                break;
        }
    }

    private void updateBoard(String board) {
        for (int i = 0; i < 9 && i < board.length(); i++) {
            char c = board.charAt(i);
            if (c != '-') {
                cells[i].setText(String.valueOf(c));
                cells[i].setEnabled(false);
            }
        }
    }

    private void handleGameResult(String[] parts) {
        // format: GAME_RESULT|gameId|WIN/LOSE/DRAW|username/...
        String type = parts[2];

        String msg;
        if (type.equals("DRAW")) {
            msg = "Game berakhir seri.";
        } else if (type.equals("WIN")) {
            String winner = parts[3];
            msg = winner.equals(youSymbol) || winner.equals("YOU") ? "Kamu menang!" : "Kamu kalah!";
        } else if (type.equals("LOSE")) {
            msg = "Kamu kalah.";
        } else {
            msg = "Game selesai.";
        }

        JOptionPane.showMessageDialog(this, msg);

        // balik ke lobby
        lobby.returnFromGame();
        dispose();
    }

    @Override
    public void onConnectionLost() {
        JOptionPane.showMessageDialog(this, "Koneksi ke server terputus.");
        lobby.setVisible(true);
        dispose();
    }
}
