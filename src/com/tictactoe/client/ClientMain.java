package com.tictactoe.client;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        try {
            // Look & feel sistem (konsisten semua window)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
