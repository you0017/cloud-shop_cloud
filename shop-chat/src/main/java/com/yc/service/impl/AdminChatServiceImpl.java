package com.yc.service.impl;

import com.yc.api.UserInformationClient;
import com.yc.bean.Chat;
import com.yc.bean.UserInformation;
import com.yc.context.BaseContext;
import com.yc.service.AdminChatService;
import com.yc.utils.WebSocket;
import com.yc.utils.YcConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class AdminChatServiceImpl implements AdminChatService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserInformationClient userInformationClient;
    @Autowired
    private WebSocket webSocket;

    @Override
    public List<List<Chat>> getAllChat() {
        Set<String> keys = redisTemplate.keys("chat_*"); // 拿到所有用户
        List<List<Chat>> allChats = new ArrayList<>();

        for (String key : keys) {
            List<Chat> chats = new ArrayList<>();
            long userLength = redisTemplate.opsForZSet().size(key); // 聊天记录个数
            Set<String> messageSet = redisTemplate.opsForZSet().range(key, 0, userLength - 1);
            List<String> chatMessages = new ArrayList<>(messageSet); // 每个用户聊天记录组成集合

            if (chatMessages != null) {
                for (String message : chatMessages) {
                    Chat chat = new Chat();
                    chat.setTime(message.substring(0, message.indexOf("^*^")));
                    chat.setSender(message.substring(message.indexOf("^*^") + 3, message.indexOf("*^*")));
                    chat.setMessage(message.substring(message.indexOf("*^*") + 3));
                    chats.add(chat);
                }
            }

            allChats.add(chats);
        }
        return allChats;
    }

    @Override
    public void clear() {
        String user_id = BaseContext.getCurrentId();
        redisTemplate.delete(YcConstants.CHAT + user_id);
    }

    @Override
    public void setChatMessage(String newMessage, String sender) {
        UserInformation userInformation = userInformationClient.getIdByAccountName(sender);// 用户id
        Integer id = userInformation.getId();

        // 获取当前时间的毫秒数
        long currentTimeMillis = System.currentTimeMillis();
        // 定义时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 将当前时间格式化为字符串
        String formattedDate = sdf.format(new Date(currentTimeMillis));

        // 添加聊天记录
        redisTemplate.opsForZSet().add(YcConstants.CHAT + id, formattedDate + "^*^" + "店家" + "*^*" + newMessage, currentTimeMillis);

        // 通知用户

        try {
            webSocket.send("1");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Chat> get() {
        String id = BaseContext.getCurrentId();


        // 获取聊天记录总数
        Long zcard = redisTemplate.opsForZSet().size(YcConstants.CHAT + id);
        // 获取聊天记录
        Set<String> messages = redisTemplate.opsForZSet().range(YcConstants.CHAT + id, 0, zcard != null ? zcard - 1 : 0);

        System.out.println(messages);

        // 获取用户姓名
        String name = userInformationClient.getById(Integer.valueOf(id)).getName();
        List<Chat> chats = new ArrayList<>();

        // 处理聊天记录
        for (String m : messages) {
            Chat chat = new Chat();
            chat.setTime(m.substring(0, m.indexOf("^*^")));
            chat.setSender(m.substring(m.indexOf("^*^") + 3, m.indexOf("*^*")));
            chat.setMessage(m.substring(m.indexOf("*^*") + 3));
            chats.add(chat);
        }
        return chats;
    }

    @Override
    public Chat getMessageById(String id) {

        String userKey = YcConstants.CHAT + id;

        // 获取聊天记录长度
        Long userLength = redisTemplate.opsForZSet().size(userKey);

        // 获取最新一条聊天记录
        Set<String> message = redisTemplate.opsForZSet().range(userKey, userLength != null && userLength > 0 ? userLength - 1 : 0, userLength != null && userLength > 0 ? userLength - 1 : 0);

        Chat msg = null;
        for (String m : message) {
            msg = new Chat();
            msg.setTime(m.substring(0, m.indexOf("^*^")));
            msg.setSender(m.substring(m.indexOf("^*^") + 3, m.indexOf("*^*")));
            msg.setMessage(m.substring(m.indexOf("*^*") + 3));
        }
        return msg;
    }
}
