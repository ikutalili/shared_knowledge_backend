package com.yuki.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CaptchasUtils {
    String from = "FUTABA<13005392970@163.com>";
//    String to = "1065079677@qq.com";
    String subject = "ValidationCode";

    @Autowired
    private JavaMailSender mailSender;  //发送邮件的接口
    @Autowired
    private RedisUtils redisUtils;
    public void sendCodeToEmail(String toEmail) {

        Random random = new Random();
        int num = 1000 + random.nextInt(900000);

        String content = "你的一次性验证码为："+ num +" 有效期 3 分钟，若不是你的操作，请忽略。";

//        创建SimpleMailMessage对象，填写基本信息
        SimpleMailMessage message = new SimpleMailMessage();
//        发送人
        message.setFrom(from);
//        接收人
        message.setTo(toEmail);
//        邮件主题
        message.setSubject(subject);
//        邮件内容
        message.setText(content);

//        发送邮件
        mailSender.send(message);
//        存入redis数据库，并设置一分钟过期
        redisUtils.storeValue(toEmail,String.valueOf(num));

    }

    /**
     *
     * @param code 输入验证码，验证是否有效
     * @return 返回布尔值
     */
    public boolean validateCode(String email,String code) {
      return redisUtils.validateValue(email,code);
    }
    public void removeCode(String email) {
        redisUtils.removeValue(email);
    }
}
