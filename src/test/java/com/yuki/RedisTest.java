package com.yuki;

import com.yuki.Utils.RedisUtils;
import com.yuki.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Map;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private UserMapper userMapper;

    @Test
    void getAllHashData() {
        Map<Object, Object> allHashData = redisUtils.getAllHashData("operateToArticle:12");
        System.out.println(allHashData);
    }
    @Test
    void getAllUserId() {
        Integer[] allUserId = userMapper.getAllUserId();
        System.out.println(Arrays.toString(allUserId));
    }
}
