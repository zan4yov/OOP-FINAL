package com.tictactoe.client;

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class ConnectionManager {

    public interface MessageListener {
        void onMessageReceived(String message);
        void onConnectionLost();
    }

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private volatile MessageListener messageListener;

    private final ExecutorService receiverExecutor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    public ConnectionManager(MessageListener listener) {
        this.messageListener = listener;
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            startReceiver();
            startHeartbeat();
            return true;

        } catch (IOException e) {
            System.err.println("[CLIENT] Failed to connect: " + e.getMessage());
            return false;
        }
    }

    private void startReceiver() {
        receiverExecutor.submit(() -> {
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    final String msg = line; // penting: final untuk lambda

                    SwingUtilities.invokeLater(() -> {
                        MessageListener listener = messageListener;
                        if (listener != null) {
                            listener.onMessageReceived(msg);
                        }
                    });
                }
            } catch (IOException e) {
                System.err.println("[CLIENT] Connection lost: " + e.getMessage());
            } finally {
                SwingUtilities.invokeLater(() -> {
                    MessageListener listener = messageListener;
                    if (listener != null) {
                        listener.onConnectionLost();
                    }
                });
            }
        });
    }

    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> sendMessage("PING"),
                5, 5, TimeUnit.SECONDS);
    }

    public void sendMessage(String msg) {
        if (output != null) {
            output.println(msg);
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException ignored) {}
    }
}
