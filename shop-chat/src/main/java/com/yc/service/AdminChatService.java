package com.yc.service;

import com.yc.bean.Chat;

import java.util.List;

public interface AdminChatService {
    public List<List<Chat>> getAllChat();

    public void clear();

    public void setChatMessage(String newMessage, String sender);

    public List<Chat> get();

    public Chat getMessageById(String id);
}
