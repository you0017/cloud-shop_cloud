package com.yc.utils;

import com.yc.bean.UserInformation;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @ClassName: Sendmail
 * @Description: Sendmail类继承Thread，因此Sendmail就是一个线程类，这个线程类用于给指定的用户发送Email
 * 发送邮件是一件非常耗时的事情，因此这里设计一个线程类来发送邮件
 * @author: hdb
 *
 */
@Component
@Data
@ConfigurationProperties(prefix = "spring.mail")
public class Sendmail extends Thread {
    //用于给用户发送邮件的邮箱
    //@Value("${spring.mail.username}")
    private String from;
    //邮箱的用户名
    //@Value("${spring.mail.username}")
    private String username;
    //邮箱的密码
    //@Value("${spring.mail.password}")
    private String password;
    //发送邮件的服务器地址
    //@Value("${spring.mail.host}")
    private String host;

    private UserInformation user;


    /* 重写run方法的实现，在run方法中发送邮件给指定的用户
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try{
            Properties prop = new Properties();
            prop.setProperty("mail.host", host);
            prop.setProperty("mail.transport.protocol", "smtp");
            prop.setProperty("mail.smtp.auth", "true");
            Session session = Session.getInstance(prop);
            session.setDebug(true);
            Transport ts = session.getTransport();
            ts.connect(host, username, password);
            Message message = createEmail(session,user);
            ts.sendMessage(message, message.getAllRecipients());
            System.out.println("发送邮件成功...");
            ts.close();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Method: createEmail
     * @Description: 创建要发送的邮件
     * @Anthor:hdb
     *
     * @param session
     * @param user
     * @return
     * @throws Exception
     */
    public Message createEmail(Session session,UserInformation user) throws Exception{

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
        message.setSubject("用户注册邮件");
                                                //+ user.getUsername() +        user.getPassword()
        String info = "恭喜您注册成功，您的用户名：" + user.getAccountname() + "，请妥善保管，如有问题请联系网站客服!";
        message.setContent(info, "text/html;charset=UTF-8");
        message.saveChanges();
        return message;
    }
}