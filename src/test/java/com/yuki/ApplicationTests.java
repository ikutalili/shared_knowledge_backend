package com.yuki;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yuki.Utils.JwtUtils;
import com.yuki.entity.User;
import com.yuki.mapper.UserMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.javassist.expr.Instanceof;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ApplicationTests {

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    @Autowired
    private RedisTemplate redisTemplate;
//    @Autowired
//    private RedisTemplate<String,Object> template;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    void contextLoads() {
        System.out.println(111);
    }
    @Test
    void set() {
        ValueOperations<String,Object> ops = redisTemplate.opsForValue();
        ops.set("age",12);
    }
    @Test
    void get() {
        ValueOperations ops = redisTemplate.opsForValue();
//        ops.set("age",12);
        Object age = ops.get("age");
        System.out.println(age);
    }

    @Test
    void set01() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("name","yuki",5, TimeUnit.SECONDS);
    }
    @Test
    void get01() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String name = ops.get("kakakak");
        System.out.println("---------------"+name);
    }
    @Test
    void loadFile() throws MalformedURLException {
        Path imgPath = Paths.get("D:\\Tokyo\\19075.jpg");

        Resource resource = new UrlResource(imgPath.toUri());
        UUID uuid = UUID.randomUUID();

//        Resource resource1 = new UrlResource((imgPath.toString()));
//        System.out.println(resource+"\n");
//        System.out.println(resource1);
    }
    @Test
    void passwordEncrypt() {
        String pwd = "Lin1208520+";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encryptedPassword = encoder.encode(pwd);
        System.out.println(encryptedPassword);

//        boolean isMatch = encoder.matches(rawPassword, encodedPassword);
    }
//    test jwt
    @Test
    void testGen() {
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",1);
        claims.put("userName","yuki");

        String token = JWT.create()
                .withClaim("claims",claims)// 使用了一个名为 "user" 的声明，并将其值设置为 claims。
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .sign(Algorithm.HMAC256("Natsuki"));
        System.out.println(token);
    }
    @Test
    void parse() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
                ".eyJ1c2VyIjp7InVzZXJOYW1lIjoieXVraSIsInVzZXJJZCI6MX0sImV4cCI6MTcwODg1MDU3NH0" +
                ".wN1Ny402L2Mc0RBBgysz_WPbb98YefqT00hdm7Xtb8o";

        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("Natsuki")).build();// 生成一个jwt对象
        DecodedJWT decodedJWT = jwtVerifier.verify(token);
        Map<String, Claim> claim = decodedJWT.getClaims();
//        System.out.println(claim.get("user"));
        System.out.println(decodedJWT);
    }
    @Autowired
    private UserMapper userMapper;
//    @Test
//    void getSomeField() {
//        User user = userMapper.selectSomeField(1);
//        System.out.println(user.toString());
//    }
    @Test
    void testMybatis()  {
        Map<String, Object> map = JwtUtils.parseTokenToGetUserInfo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
                ".eyJjbGFpbXMiOnsibmFtZSI6bnVsbCwidXNlcklkIjo3fSwiZXhwIjoxNzA5MDA4NzkwfQ" +
                ".Zo6tv6jnTuvbbMU3eLxqBU-1G2F7dIf5vRgQXvYWQMA");
        System.out.println(map.get("userId"));
//        System.out.println(map);
    }

    @Test
    void testMybatis1() {
        User user = userMapper.testMultiJoin1();
        System.out.println(user.getPassword());

//        User user1 = userMapper.selectOne(1);
//        System.out.println(user1.toString());
    }
    private String PASSWORD_LENGTH_PATTERN = "^.{8,16}$";
    String email = "1065023@gmail.com";
    @Test
    void testRegex() {
        System.out.println("hit54fg78".matches(PASSWORD_LENGTH_PATTERN));
        System.out.println(email.matches(EMAIL_PATTERN));
    }
}
