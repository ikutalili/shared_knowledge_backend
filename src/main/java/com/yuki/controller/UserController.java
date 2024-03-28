package com.yuki.controller;

import com.yuki.Utils.CaptchasUtils;
import com.yuki.Utils.JwtUtils;
import com.yuki.Utils.PasswordUtils;
import com.yuki.Utils.RedisUtils;
import com.yuki.entity.Result;
import com.yuki.entity.User;
import com.yuki.mapper.UserMapper;
import com.yuki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private CaptchasUtils captchasUtils;
    @Autowired
    private RedisUtils redisUtils;

    private static final String PASSWORD_LENGTH_PATTERN = "^.{8,16}$";
    private static final String USERNAME_LENGTH_PATTERN = "^.{1,100}$";
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private boolean isLogin(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        return user != null;
    }
    @RequestMapping("my-info")
    public Result myInfo(/*@RequestHeader(name = "Authorization") String token*/HttpServletRequest request) {
//        1.若接收的token为空，则返回错误
//        if (token == null || token.isEmpty()){
//            System.out.println("token is null");
//            return Result.error();
//
//        }
////        2.若token不为空，但已经过期，返回错误
//        else {
//            System.out.println(token);
//            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
//            String redisToken = operations.get(token); // 如果redis中token与前端传过来的相同，则查询不为空
//            if (redisToken == null) {
//                // 若为空。证明用户未登录或登录已过期。
//                return Result.error();
//            }
////            token未过期，返回用户数据
//            else {
////            数据库中查询用户信息并返回
//                Map<String, Object> claims = JwtUtils.parseTokenToGetUserInfo(token);
//                User user = userService.getUserInfoById((Integer) claims.get("userId"));
////                userService.getUserInfoByToken(token);
//                return Result.successWithData(user);
//
//            }
//        }
        User user = (User) request.getAttribute("user");
        if (user != null) {
            return Result.successWithData(user);
        }
        else {
            return Result.error();
        }

    }
    @PostMapping("login")
    public Result<Map<String,User>> userLogin(String email,String password) {
        String token = userService.userLogin(email, password);
        if (token != null) {
            User user = userService.getUserInfoByToken(token);
            Map<String,User> map = new HashMap<>();
            map.put(token,user);
            return Result.successWithData(map);
        }
        else {
            return Result.error();  //只要是正确的数据，就不会返回错误信息
        }

    }
    @PostMapping("register")
    public Result<Map<String,User>> userRegister(String userName,String email,String password,Integer gender,String captcha) {

        if (userName != null && email != null && password != null && captcha != null && (gender == 0 || gender == 1)) {
            String token = userService.addUser(userName, email, password, gender,captcha);
            if (token.equals("dataError")) {
//                System.out.println("1------"+token);
                return Result.error();

            }
            else if(token.equals("emailInUsed")) {
                System.out.println("2----------"+token);
                return new Result<>(2,"emailInUsed",null);
            }
            else {
                User user = userService.getUserInfoByToken(token);
                Map<String,User> map = new HashMap<>();
                map.put(token,user);
                System.out.println("3------" + token);
                return Result.successWithData(map);
            }
        }
       else {
           return Result.error();
        }

    }
    @PostMapping("Captcha")
    public Result<Void> sendCaptcha(String email) {
        if (email == null || email.isEmpty()) {
            return Result.error();
        } else {
            captchasUtils.sendCodeToEmail(email);
            return Result.successWithoutData();
        }
    }

//    验证用户是否登录的接口
    @GetMapping("checkUserLoggedIn")
    public Result<Void> checkUserLoggedIn(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        if (token == null || token.isEmpty()){
            return Result.error();
        }
//        验证token是否已经过期,虽然可以本来可以设置过期时间，但是有时需要立即过期，就需要redis来确定是否过期
        boolean redisToken = redisUtils.validateToken(token);
//        验证token是否被篡改过
        boolean validToken = JwtUtils.isValidToken(redisUtils.getToken(token)); //验证token是否正确，即有没有被篡改过或者错误或者过期
        if (redisToken && validToken) {
            return Result.successWithoutData();
        }
        else {
            return Result.error();
        }
    }

    @GetMapping("logout")
    public Result<Void> logOut(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        System.out.println("logout-token" + token);
        Boolean logout = redisUtils.removeToken(token);
        if (logout == null || !logout) {
            return Result.error();
        }
        else {
            return Result.successWithoutData();
        }
    }

//    更新用户信息
    @PutMapping("updateInfo")
    Result<User> updateUserInfo(HttpServletRequest request,@RequestParam(required = false) String profile,@RequestParam(required = false) String userName,@RequestParam(required = false) String password,@RequestParam(required = false) String captcha ) {
//        Map<String, Object> claims = JwtUtils.parseTokenToGetUserInfo(token);
//        Integer userId = (Integer) claims.get("userId");
        User user1 = (User) request.getAttribute("user");
        String token = (String) request.getAttribute("token");
        User user = userService.editUserInfo(user1.getUserId(), userName, password, captcha,token,profile);
        System.out.println(user == null);
        if (user == null) {
            return Result.error();
        }
        else {
            return Result.successWithData(user);
        }
    }
    @PutMapping("change-password")
    Result<Void> changePassword(@RequestParam String email,@RequestParam String password,@RequestParam String captcha) {
            if ( (password != null && !password.isEmpty() ) && ( captcha != null && !captcha.isEmpty() ) && captchasUtils.validateCode(email,captcha)) {
                try {
//                验证码验证不成功，token没有失效
                    System.out.println("验证码是否有效"+captchasUtils.validateCode(email,captcha));
                    userMapper.updateUserPasswordByEmail(email, PasswordUtils.passwordEncrypt(password));
                    return Result.successWithoutData();
                }catch (Exception e){
                    e.printStackTrace();
                    return Result.error();
                }
            }
        return Result.error();
    }

    @GetMapping("get-all-users")
    Result<List<User>> getAllUserInfo(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user != null && user.getRole().equals("2")) {
            List<User> allUsers = userService.getAllUsers();
            return Result.successWithData(allUsers);
        }
        else {
            return Result.error();
        }
    }

    @PutMapping("follow-user/{userId}/{followingId}")
    Result<Void> followUser(@PathVariable String userId,@PathVariable String followingId,HttpServletRequest request) {
        if (isLogin(request)) {
            try {
                userService.followUser(userId,followingId);
                return Result.successWithoutData();
            }catch (Exception e) {
                e.printStackTrace();
                return Result.error();
            }
        }
        return Result.error();
    }
    @PutMapping("unfollow-user/{userId}/{followingId}")
    Result<Void> unfollowUser(@PathVariable String userId,@PathVariable String followingId,HttpServletRequest request) {
        if (isLogin(request)) {
            try {
                userService.unfollowUser(userId,followingId);
                return Result.successWithoutData();
            }catch (Exception e) {
                e.printStackTrace();
                return Result.error();
            }
        }
        return Result.error();
    }

    @DeleteMapping("delete-user/{userId}")
    public Result<Void> deleteUser(@PathVariable Integer userId,HttpServletRequest request) {
        if (isLogin(request)) {
            try {
                userService.deleteUser(userId);
                return Result.successWithoutData();
            }catch (Exception e) {
                return Result.error();
            }
        }
        return Result.error();
    }
}
