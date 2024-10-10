package com.yc.service.impl;

import com.yc.api.UserInformationClient;
import com.yc.bean.Chat;
import com.yc.bean.UserInformation;
import com.yc.context.BaseContext;
import com.yc.service.ChatService;
import com.yc.utils.AdminEchoServer;
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
import java.util.concurrent.TimeUnit;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserInformationClient userInformationClient;
    @Autowired
    private AdminEchoServer adminEchoServer;

    @Override
    public void clear() {
        String user_id = BaseContext.getCurrentId();
        redisTemplate.delete(YcConstants.CHAT+user_id);
    }

    @Override
    public void send(String id, String name, String message) {
        /**
         * 键 - 时间戳 - 内容
         */
        // 获取当前时间的毫秒数
        long currentTimeMillis = System.currentTimeMillis();
        // 创建一个 Date 对象
        Date date = new Date(currentTimeMillis);
        // 定义时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 将 Date 对象格式化为字符串
        String formattedDate = sdf.format(date);

        String key = YcConstants.CHAT + id;

        // 添加到有序集合
        redisTemplate.opsForZSet().add(key, formattedDate + "^*^" + name + "*^*" + message, currentTimeMillis);

        // 设置过期时间
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.expire(key, 1, TimeUnit.DAYS); // 设置过期时间为 1 天
        }


        //已经把消息存到redis了
        //现在连接webSocket给后台的前端发送消息
        //String url = generateUrl(req);

        try {
            adminEchoServer.send(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //给连接我的这个前台发送id，告诉要刷新哪一个用户了
    }

    @Override
    public List<Chat> get() {
        String id = BaseContext.getCurrentId();
        String key = YcConstants.CHAT + id;

        // 获取 ZSet 中的元素数量
        Long zcard = redisTemplate.opsForZSet().size(key);

        Set<String> messageSet = redisTemplate.opsForZSet().range(key, 0, zcard - 1);
        List<String> message = new ArrayList<>(messageSet); // 注意这里是 zcard - 1

        UserInformation user = userInformationClient.getById(Integer.valueOf(id));
        String name = user.getName();

        List<Chat> chats = new ArrayList<>();
        Chat chat = null;
        for (String m : message) {
            chat = new Chat();
            chat.setTime(m.substring(0,m.indexOf("^*^")));
            chat.setSender(m.substring(m.indexOf("^*^")+3,m.indexOf("*^*")));
            chat.setMessage(m.substring(m.indexOf("*^*")+3));
            chats.add(chat);
        }
        return chats;
    }
}
