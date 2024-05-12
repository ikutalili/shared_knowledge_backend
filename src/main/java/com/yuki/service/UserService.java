package com.yuki.service;

import com.yuki.entity.SocialNetwork;
import com.yuki.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {
//    User getUserInfoByEmail(String email);
    User getUserInfoById(Integer userId);
    User getUserInfoByToken(String token);

//    如果用户注册成功，则返回token
    String addUser(String userName,String email,String password,Integer gender,String Captcha);

// 用户登录接口，如果登录成功，则下发token
    String userLogin(String email,String password);

//    修改用户头像
    void updateUserAvatar(Integer userId,String avatarUrl);

//    修改用户信息
    User editUserInfo(Integer userId,String userName,String password,String captcha,String oldToken,String profile);

//    获取用户头像文件名
    String getAvatarFileName(Integer userId);

//    检查该邮箱是否已在数据库中存在
    User checkEmailExists(String email);
//
    List<User> getAllUsers();

//    关注用户
    void followUser(String userId,String followingId);

//    取消关注
    void unfollowUser(String userId,String followingId);

//    删除/注销用户
    void deleteUser(Integer id);

//    获取所有用户id
    Integer[] getAllUserId();

    SocialNetwork getUserRelationShip(String userId);
}
