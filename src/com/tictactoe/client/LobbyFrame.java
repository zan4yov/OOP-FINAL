package com.tictactoe.client;

import javax.swing.*;
import java.awt.*;

public class LobbyFrame extends JFrame implements ConnectionManager.MessageListener {

    private final ConnectionManager connectionManager;
    private final String username;

    private DefaultListModel<String> userListModel = new DefaultListModel<>();
    private JList<String> userList = new JList<>(userListModel);

    private JTextArea chatArea = new JTextArea();
    private JTextField chatInput = new JTextField();

    public LobbyFrame(ConnectionManager cm, String username) {
        this.connectionManager = cm;
        this.username = username;

        // Listener harus aktif sebelum UI
        cm.setMessageListener(this);

        initUI();
    }

    private void initUI() {
        setTitle("Lobby - " + username);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // LEFT PANEL - user list + buttons
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(200, 0));

        JButton inviteButton = new JButton("Invite");
        inviteButton.addActionListener(e -> inviteSelectedUser());

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> logout());

        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 6, 6));
        bottomButtons.add(inviteButton);
        bottomButtons.add(quitButton);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(userScroll, BorderLayout.CENTER);
        leftPanel.add(bottomButtons, BorderLayout.SOUTH);

        // CHAT AREA
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        chatInput.addActionListener(e -> sendChat());

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(chatPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // ========== BUTTON ACTIONS ==========

    private void inviteSelectedUser() {
        String target = userList.getSelectedValue();
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Pilih user dulu!");
            return;
        }
        if (target.equals(username)) {
            JOptionPane.showMessageDialog(this, "Tidak bisa invite diri sendiri.");
            return;
        }

        connectionManager.sendMessage("INVITE|" + target);
        chatArea.append("[INFO] Invite dikirim ke " + target + "\n");
    }

    private void sendChat() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) {
            connectionManager.sendMessage("CHAT_GLOBAL|" + msg);
            chatInput.setText("");
        }
    }

    // ========== LOGOUT / QUIT FEATURE ==========

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to quit?",
                "Quit",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            try {
                connectionManager.sendMessage("QUIT");
                connectionManager.disconnect();
            } catch (Exception ignored) {}

            // kembali ke login
            LoginFrame login = new LoginFrame();
            login.setVisible(true);

            this.dispose();
        }
    }

    // Called oleh GameFrame setelah selesai
    public void returnFromGame() {
        connectionManager.setMessageListener(this);
        this.setVisible(true);
    }

    // ========== MESSAGE HANDLER ==========

    @Override
    public void onMessageReceived(String message) {
        String[] parts = message.split("\\|");
        String cmd = parts[0];

        switch (cmd) {

            case "USER_LIST":
                userListModel.clear();
                if (parts.length > 1) {
                    for (String user : parts[1].split(",")) {
                        if (!user.isBlank())
                            userListModel.addElement(user);
                    }
                }
                break;

            case "CHAT_GLOBAL_FROM":
                chatArea.append(parts[1] + ": " + parts[2] + "\n");
                break;

            case "INVITE_FROM":
                handleInvite(parts[1]);
                break;

            case "INVITE_ACCEPTED":
                chatArea.append("[INFO] " + parts[1] + " menerima invite kamu.\n");
                break;

            case "INVITE_DECLINED":
                chatArea.append("[INFO] " + parts[1] + " menolak invite kamu.\n");
                break;

            case "GAME_START":
                openGame(parts);
                break;

            default:
                // abaikan message lain
                break;
        }
    }

    private void handleInvite(String from) {
        int r = JOptionPane.showConfirmDialog(
                this,
                from + " invited you to play. Accept?",
                "Invite",
                JOptionPane.YES_NO_OPTION
        );

        if (r == JOptionPane.YES_OPTION) {
            connectionManager.sendMessage("ACCEPT_INVITE|" + from);
        } else {
            connectionManager.sendMessage("DECLINE_INVITE|" + from);
        }
    }

    private void openGame(String[] parts) {
        String gameId = parts[1];
        String youSymbol = parts[2].split("=")[1];
        String opponent = parts[3].split("=")[1];

        GameFrame gameFrame = new GameFrame(connectionManager, this, gameId, youSymbol, opponent);

        // Listener pindah ke GameFrame
        connectionManager.setMessageListener(gameFrame);

        this.setVisible(false);
    }

    @Override
    public void onConnectionLost() {
        JOptionPane.showMessageDialog(this, "Disconnected from server.");
        System.exit(0);
    }
}
