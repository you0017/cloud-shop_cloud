package com.yc.utils;


import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 给后台管理员的前端发送消息的
 */
@ServerEndpoint("/ws/chat/echo")
@Component
public class WebSocket {
    private static Session session;
 
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket The connection has been established");
        WebSocket.session = session;
    }
 
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("get message:" + message);
        //session.getBasicRemote().sendText("服务器收到消息：" + message);
    }
 
    @OnClose
    public void onClose() {
        System.out.println("WebSocket The connection has been closed");
    }
 
    @OnError
    public void onError(Throwable t) {
        System.out.println("WebSocket There was an error with the connection:" + t.getMessage());
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }
}