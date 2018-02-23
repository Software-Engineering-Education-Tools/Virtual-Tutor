package de.ur.mi.roberts;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;


public class LLServer extends WebSocketServer {

    private WebSocket connection;
    private ArrayList<AssistantInteractionListener> listeners;


    public LLServer(int port) {
        super(new InetSocketAddress(port));
        this.listeners = new ArrayList<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        this.connection = conn;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        message = message.trim();
        for (AssistantInteractionListener listener : listeners) {
            listener.onMessageReceived(message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occured on connection ");//+ conn.getRemoteSocketAddress());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
    }

    public void sendMessage(String message) {
        if (connection != null && connection.isOpen()) {
            connection.send(message);
            for(AssistantInteractionListener listener : listeners){
                listener.onMessageSent(message);
            }
        }
    }

    public void shutdown() {

        try {
            stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void addInteractionListener(AssistantInteractionListener listener) {
        listeners.add(listener);
    }
}