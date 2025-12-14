package com.tictactoe.client;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import com.tictactoe.client.UIStyles.RoundedButton;
import com.tictactoe.client.UIStyles.RoundedPanel;

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

        UIStyles.install();
        initUI();

        // jadikan GameFrame sebagai listener aktif
        cm.setMessageListener(this);
    }

    private void initUI() {
        setTitle("Game vs " + opponentName);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(920, 660);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        // TOP: status in a rounded panel
        RoundedPanel topPanel = new RoundedPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        turnLabel = new JLabel("Waiting for your turn...", SwingConstants.CENTER);
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        turnLabel.setForeground(UIStyles.TEXT);

        JLabel opponentLabel = new JLabel("Opponent: " + opponentName, SwingConstants.CENTER);
        opponentLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        opponentLabel.setForeground(UIStyles.TEXT);

        topPanel.add(turnLabel, BorderLayout.CENTER);
        topPanel.add(opponentLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // CENTER: board
        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setOpaque(false);
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 12, 12));
        boardPanel.setOpaque(false);
        boardPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        for (int i = 0; i < 9; i++) {
            RoundedButton btn = new RoundedButton("");
            btn.setFont(new Font("SansSerif", Font.BOLD, 42));
            btn.setPreferredSize(new Dimension(120, 120));
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFocusable(false);

            int idx = i;
            btn.addActionListener(e -> handleCellClick(idx));
            btn.setEnabled(false);

            cells[i] = btn;
            boardPanel.add(btn);
        }

        boardWrapper.add(boardPanel);
        add(boardWrapper, BorderLayout.CENTER);

        // RIGHT: chat in-game in rounded card
        chatArea.setEditable(false);
        chatArea.setForeground(UIStyles.TEXT);
        chatArea.setBackground(new Color(40, 38, 54));
        JScrollPane chatScroll = new JScrollPane(chatArea);

        chatInput.addActionListener(e -> sendGameChat());

        RoundedPanel chatCard = new RoundedPanel();
        chatCard.setLayout(new BorderLayout(8, 8));
        chatCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel chatTitle = new JLabel("Game Chat", SwingConstants.CENTER);
        chatTitle.setForeground(UIStyles.TEXT);
        chatTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        chatCard.add(chatTitle, BorderLayout.NORTH);
        chatCard.add(chatScroll, BorderLayout.CENTER);
        chatCard.add(chatInput, BorderLayout.SOUTH);

        chatCard.setPreferredSize(new Dimension(300, 0));
        add(chatCard, BorderLayout.EAST);

        setVisible(true);
    }

    private void handleCellClick(int idx) {
        cm.sendMessage("GAME_MOVE|" + gameId + "|" + idx);

        // Setelah mengambil langkah → harus menunggu
        turnLabel.setText("Wait your turn...");
        turnLabel.setForeground(Color.RED.darker());

        enableBoard(false);
    }

    private void sendGameChat() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty())
            return;

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
                cells[i].setEnabled(false); // Occupied cells always disabled
            } else {
                cells[i].setText(""); // Clear text if it became empty
                // Note: enableBoard() will be called when YOUR_TURN arrives
                // or if we need to refresh state.
                // But generally, buttons are enabled via enableBoard().
                // However, we should ensure that if it IS our turn, empty cells are enabled.
            }
        }

        // If it is currently my turn (label is green/My Turn),
        // I need to make sure empty cells are clickable.
        if (turnLabel.getText().startsWith("Your turn")) {
            enableBoard(true);
        }
    }

    private void enableBoard(boolean enable) {
        for (JButton btn : cells) {
            String text = btn.getText();
            // Only enable empty cells
            if (text == null || text.trim().isEmpty()) {
                btn.setEnabled(enable);
            } else {
                btn.setEnabled(false);
            }
        }
    }

    private void handleGameResult(String[] parts) {
        // format: GAME_RESULT|gameId|WIN/LOSE/DRAW|username/...
        String type = parts[2];

        String msg;
        if (type.equals("DRAW")) {
            msg = "Game berakhir seri.";
        } else if (type.equals("WINNER")) {
            String winner = parts[3];
            if (winner.equals(opponentName)) {
                msg = "Kamu kalah.";
            } else {
                msg = "Kamu menang!";
            }
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
