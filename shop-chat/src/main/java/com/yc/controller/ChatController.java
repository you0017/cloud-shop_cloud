package com.yc.controller;


import com.yc.bean.Chat;
import com.yc.model.JsonModel;
import com.yc.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

//@WebServlet("/html/chat.action")
@RestController
@RequestMapping("/chat")
public class ChatController {


    @Autowired
    private ChatService chatService;

    /**
     * 清除这次聊天记录
     */
    @DeleteMapping("/clear")
    public JsonModel clear() {
        chatService.clear();
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        return jm;
    }

    /**
     * 发送
     */
    @PostMapping("/send")
    public JsonModel send(@RequestParam("id")String id,
                          @RequestParam("name")String name,
                          @RequestParam("message")String message) {
        JsonModel jm = new JsonModel();

        chatService.send(id, name, message);
        jm.setCode(1);
        return jm;
    }


    @GetMapping("/get")
    public JsonModel get() {

        List<Chat> chats = chatService.get();

        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(chats);
        return jm;
    }
}
