package com.tictactoe.client;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import com.tictactoe.client.UIStyles.RoundedButton;
import com.tictactoe.client.UIStyles.RoundedPanel;

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

        UIStyles.install();
        cm.setMessageListener(this);

        initUI();
    }

    private void initUI() {
        setTitle("Tic-Tac-Toe Lobby - " + username);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 660);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        // LEFT: user list card
        RoundedPanel leftCard = new RoundedPanel();
        leftCard.setLayout(new BorderLayout(8, 8));
        leftCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        leftCard.setPreferredSize(new Dimension(260, 0));

        JLabel onlineLabel = new JLabel("Online Users", SwingConstants.CENTER);
        onlineLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        onlineLabel.setForeground(UIStyles.TEXT);

        JScrollPane userScroll = new JScrollPane(userList);
        userList.setBackground(new Color(44, 42, 60));
        userList.setForeground(UIStyles.TEXT);

        JPanel leftButtons = new JPanel(new GridLayout(2, 1, 8, 8));
        leftButtons.setOpaque(false);
        RoundedButton inviteBtn = new RoundedButton("Invite to Play");
        inviteBtn.addActionListener(e -> inviteSelectedUser());
        RoundedButton quitBtn = new RoundedButton("Quit");
        quitBtn.addActionListener(e -> logout());
        leftButtons.add(inviteBtn);
        leftButtons.add(quitBtn);

        leftCard.add(onlineLabel, BorderLayout.NORTH);
        leftCard.add(userScroll, BorderLayout.CENTER);
        leftCard.add(leftButtons, BorderLayout.SOUTH);

        // CENTER: chat card
        RoundedPanel chatCard = new RoundedPanel();
        chatCard.setLayout(new BorderLayout(8, 8));
        chatCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel chatLabel = new JLabel("Global Chat", SwingConstants.CENTER);
        chatLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        chatLabel.setForeground(UIStyles.TEXT);

        chatArea.setEditable(false);
        chatArea.setBackground(new Color(40, 38, 54));
        chatArea.setForeground(UIStyles.TEXT);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        chatInput.addActionListener(e -> sendChat());
        RoundedButton sendBtn = new RoundedButton("Send");
        sendBtn.addActionListener(e -> sendChat());

        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setOpaque(false);
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        chatCard.add(chatLabel, BorderLayout.NORTH);
        chatCard.add(chatScroll, BorderLayout.CENTER);
        chatCard.add(inputPanel, BorderLayout.SOUTH);

        // Footer
        JLabel footer = new JLabel("Welcome to the lobby!", SwingConstants.CENTER);
        footer.setForeground(UIStyles.PRIMARY);

        add(leftCard, BorderLayout.WEST);
        add(chatCard, BorderLayout.CENTER);
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
