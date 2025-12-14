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

        cm.setMessageListener(this);

        initUI();
    }

    private void initUI() {
        setTitle("Tic-Tac-Toe Lobby - " + username);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== LEFT PANEL =====
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 0));

        JLabel onlineLabel = new JLabel("Online Users", SwingConstants.CENTER);
        onlineLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        onlineLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane userScroll = new JScrollPane(userList);

        JButton inviteBtn = new JButton("Invite to Play");
        inviteBtn.addActionListener(e -> inviteSelectedUser());

        JButton quitBtn = new JButton("Quit");
        quitBtn.addActionListener(e -> logout());

        JPanel bottomButtons = new JPanel(new GridLayout(2, 1, 6, 6));
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomButtons.add(inviteBtn);
        bottomButtons.add(quitBtn);

        leftPanel.add(onlineLabel, BorderLayout.NORTH);
        leftPanel.add(userScroll, BorderLayout.CENTER);
        leftPanel.add(bottomButtons, BorderLayout.SOUTH);

        // ===== RIGHT PANEL =====
        JPanel rightPanel = new JPanel(new BorderLayout());

        JLabel chatLabel = new JLabel("Global Chat", SwingConstants.CENTER);
        chatLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        chatLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        chatInput.addActionListener(e -> sendChat());
        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> sendChat());

        JPanel inputPanel = new JPanel(new BorderLayout(6, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        JLabel footer = new JLabel("Welcome to the lobby!", SwingConstants.CENTER);
        footer.setForeground(Color.BLUE);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        rightPanel.add(chatLabel, BorderLayout.NORTH);
        rightPanel.add(chatScroll, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
        connectionManager.sendMessage("REQ_USER_LIST");
    }

    // ======= BUTTON ACTIONS =======

    private void inviteSelectedUser() {
        String target = userList.getSelectedValue();
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Select a user to invite.");
            return;
        }
        if (target.equals(username)) {
            JOptionPane.showMessageDialog(this, "Cannot invite yourself.");
            return;
        }

        connectionManager.sendMessage("INVITE|" + target);
        chatArea.append("[INFO] Invite sent to " + target + "\n");
    }

    private void sendChat() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) {
            connectionManager.sendMessage("CHAT_GLOBAL|" + msg);
            chatInput.setText("");
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to quit?",
                "Quit",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                connectionManager.sendMessage("QUIT");
                connectionManager.disconnect();
            } catch (Exception ignored) {
            }

            LoginFrame login = new LoginFrame();
            login.setVisible(true);
            this.dispose();
        }
    }

    // ======= GAME RETURN =======
    public void returnFromGame() {
        connectionManager.setMessageListener(this);
        this.setVisible(true);
    }

    // ======= RECEIVE MESSAGE =======

    @Override
    public void onMessageReceived(String message) {
        String[] p = message.split("\\|");
        String cmd = p[0];

        switch (cmd) {

            case "USER_LIST":
                userListModel.clear();
                if (p.length > 1) {
                    for (String u : p[1].split(",")) {
                        if (!u.isBlank() && !u.equals(username))
                            userListModel.addElement(u);
                    }
                }
                break;

            case "CHAT_GLOBAL_FROM":
                chatArea.append(p[1] + ": " + p[2] + "\n");
                break;

            case "INVITE_FROM":
                handleInvite(p[1]);
                break;

            case "INVITE_ACCEPTED":
                chatArea.append("[INFO] " + p[1] + " accepted your invite.\n");
                break;

            case "INVITE_DECLINED":
                chatArea.append("[INFO] " + p[1] + " declined your invite.\n");
                break;

            case "GAME_START":
                openGame(p);
                break;
        }
    }

    private void handleInvite(String from) {
        int r = JOptionPane.showConfirmDialog(
                this,
                from + " invited you to play. Accept?",
                "Invitation",
                JOptionPane.YES_NO_OPTION);

        if (r == JOptionPane.YES_OPTION) {
            connectionManager.sendMessage("ACCEPT_INVITE|" + from);
        } else {
            connectionManager.sendMessage("DECLINE_INVITE|" + from);
        }
    }

    private void openGame(String[] p) {
        String gameId = p[1];
        String youSymbol = p[2].split("=")[1];
        String opponent = p[3].split("=")[1];

        GameFrame gameFrame = new GameFrame(connectionManager, this, gameId, youSymbol, opponent);
        connectionManager.setMessageListener(gameFrame);
        this.setVisible(false);
    }

    @Override
    public void onConnectionLost() {
        JOptionPane.showMessageDialog(this, "Disconnected from server.");
        System.exit(0);
    }
}
