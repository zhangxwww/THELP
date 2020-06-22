package com.example.websocket;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class JWebSocketClient extends WebSocketClient {
//    private static JWebSocketClient instance;
    private static String url;
    public JWebSocketClient(URI serverUri, Map<String,String> httpHeaders) {
        super(serverUri, new Draft_6455(), httpHeaders);
    }
//    public JWebSocketClient(String url) throws URISyntaxException {
//        super(new URI(url), new Draft_6455());
//        this.url = url;
//    }

//    public static JWebSocketClient getInstance(String url){
//        if (instance == null) {
//            try {
//                instance = new JWebSocketClient(url);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        }
//        return instance;
//    }
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.e("JWebSocketClient", "onOpen()");
    }

    @Override
    public void onMessage(String message) {
        Log.e("JWebSocketClient", "onMessage()");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e("JWebSocketClient", "onClose Code = " + String.valueOf(code) + "  Reason:" + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e("JWebSocketClient", "onError(): "+ex.toString());
    }
}