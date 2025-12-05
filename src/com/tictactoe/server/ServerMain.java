package com.tictactoe.server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) {
        int port = 8888;
        ServerState state = new ServerState();

        System.out.println("[SERVER] Starting...");

        // Monitoring Thread (prints active clients)
        new Thread(() -> {
            while (true) {
                try { Thread.sleep(5000); } catch (Exception ignored) {}
                System.out.println("[MONITOR] Active clients: " + state.getUserCount());
            }
        }).start();

        try (ServerSocket server = new ServerSocket(port)) {

            System.out.println("[SERVER] Running on port " + port);
            System.out.println("[SERVER] Waiting for clients...");

            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("[SERVER] New connection.");

                ClientHandler handler = new ClientHandler(clientSocket, state);
                new Thread(handler).start();
            }

        } catch (Exception e) {
            System.out.println("[SERVER ERROR] " + e.getMessage());
        }
    }
}
