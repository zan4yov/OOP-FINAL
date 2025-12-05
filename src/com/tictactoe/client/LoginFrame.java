package com.tictactoe.client;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame implements ConnectionManager.MessageListener {

    private JTextField usernameField;
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;

    private ConnectionManager connectionManager;

    public LoginFrame() {
        setTitle("Tic-Tac-Toe Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 240);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Tic-Tac-Toe", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(title, c);

        c.gridwidth = 1;

        // Username
        c.gridy = 1; c.gridx = 0;
        panel.add(new JLabel("Username"), c);
        usernameField = new JTextField("razan");
        c.gridx = 1;
        panel.add(usernameField, c);

        // Host
        c.gridy = 2; c.gridx = 0;
        panel.add(new JLabel("Server"), c);
        hostField = new JTextField("localhost");
        c.gridx = 1;
        panel.add(hostField, c);

        // Port
        c.gridy = 3; c.gridx = 0;
        panel.add(new JLabel("Port"), c);
        portField = new JTextField("8888");
        c.gridx = 1;
        panel.add(portField, c);

        // Button
        connectButton = new JButton("Connect & Login");
        connectButton.addActionListener(e -> doLogin());

        c.gridy = 4; c.gridx = 0; c.gridwidth = 2;
        panel.add(connectButton, c);

        setContentPane(panel);
    }

    private void doLogin() {
        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username tidak boleh kosong.");
            return;
        }

        connectionManager = new ConnectionManager(this);

        if (!connectionManager.connect(host, port)) {
            JOptionPane.showMessageDialog(this, "Gagal connect ke server.");
            return;
        }

        connectionManager.sendMessage("LOGIN|" + username);
        connectButton.setEnabled(false);
    }

    @Override
    public void onMessageReceived(String message) {
        String[] parts = message.split("\\|");

        switch (parts[0]) {
            case "LOGIN_OK":
                String user = parts[1];
                SwingUtilities.invokeLater(() -> {
                    LobbyFrame lobby = new LobbyFrame(connectionManager, user);
                    this.setVisible(false);
                });
                break;

            case "LOGIN_FAIL":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Login gagal: " + parts[1]);
                    connectButton.setEnabled(true);
                });
                break;

            default:
                // abaikan message lain di login
                break;
        }
    }

    @Override
    public void onConnectionLost() {
        JOptionPane.showMessageDialog(this, "Koneksi ke server terputus.");
        System.exit(0);
    }
}
