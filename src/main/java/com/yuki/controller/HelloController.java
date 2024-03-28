package com.yuki.controller;

import com.yuki.entity.Result;
import com.yuki.entity.User;
import com.yuki.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Autowired
    private UserMapper userMapper;

    @RequestMapping("/hello")
    User getUserInfoById(){
//        Result.error();
        return userMapper.selectById(1);
    }
}
