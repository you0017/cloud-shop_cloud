package com.yc.service;

import com.yc.bean.Chat;

import java.util.List;

public interface ChatService {
    public void clear();

    public void send(String id, String name, String message);

    public List<Chat> get();
}
