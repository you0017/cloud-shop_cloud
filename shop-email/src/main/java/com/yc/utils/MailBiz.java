package com.yc.utils;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailBiz {
    @Value("${spring.mail.properties.mail.smtp.from}")
    private String from;
    @Autowired
    private JavaMailSender javaMailSender;


    @Async
    public void sendMail(String to, String subject, String content) {
        //SimpleMailMessage mail = new SimpleMailMessage();//不包括附件
        MimeMessage mm = javaMailSender.createMimeMessage();//可以包括附件


        try {
            MimeMessageHelper message = new MimeMessageHelper(mm,true,"utf-8");//true代表可以有附件
            message.setFrom(from);//谁发
            message.setTo(to);//发给谁
            message.setSubject(subject);//主题
            message.setText(content,true);//内容,true代表传的是html
            javaMailSender.send(mm);//发送
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
