package com.yc.controller.admin;


import com.yc.api.UserInformationClient;
import com.yc.bean.Chat;
import com.yc.bean.UserInformation;
import com.yc.context.BaseContext;
import com.yc.model.JsonModel;
import com.yc.service.AdminChatService;
import com.yc.utils.WebSocket;
import com.yc.utils.YcConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/chat/admin")
public class AdminChatController {

    @Autowired
    private AdminChatService adminChatService;

    // 初始化读取所有消息
    @RequestMapping("/getAllChat")
    public JsonModel getAllChat() {
        JsonModel jm = new JsonModel();

        List<List<Chat>> allChats = adminChatService.getAllChat();

        jm.setCode(1);
        jm.setObj(allChats);
        return jm;
    }


    /**
     * 清除这次聊天记录
     */
    @RequestMapping("/clear")
    protected JsonModel clear() {
        adminChatService.clear();

        JsonModel jm = new JsonModel();
        jm.setCode(1);
        return jm;
    }

    /**
     * 发送
     */
    @RequestMapping("/setChatMessage")
    public JsonModel setChatMessage(@RequestParam("newMessage")String newMessage,@RequestParam("sender")String sender) {
        adminChatService.setChatMessage(newMessage, sender);
        // 返回 JSON 响应
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        return jm;
    }

    @RequestMapping("/get")
    public JsonModel get() {
        List<Chat> chats = adminChatService.get();
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(chats);
        return jm;
    }

    // 根据id获取记录
    @RequestMapping("/getMessageById")
    public JsonModel getMessageById(@RequestParam("id")String id) {
        Chat msg = adminChatService.getMessageById(id);
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(msg);
        return jm;
    }


}
