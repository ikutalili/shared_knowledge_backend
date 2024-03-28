package com.yuki;

import com.yuki.Utils.CaptchasUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailTest {

//    @Value("$")
    @Autowired
    private CaptchasUtils captchasUtils;
    @Test
    void sendCode() {
//        captchasUtils.sendCodeToEmail("biniday346@minhlun.com");
//        System.out.println(captchasUtils.validateCode("241217"));
    }


}
