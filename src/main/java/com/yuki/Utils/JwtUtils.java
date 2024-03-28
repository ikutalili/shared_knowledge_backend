package com.yuki.Utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JwtUtils {
    private static final String KEY = "Natsuki!8^%..00";

    /**
     *
     * @param claims WT token 中包含的声明
     * @param time 过期时间（单位分钟）
     * @return 返回token值
     */
    public static String genToken(Map<String, Object> claims,Integer time) {
        return JWT.create()
                .withClaim("claims",claims)// 使用了一个名为 "user" 的声明，并将其值设置为 claims。
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * time))
                .sign(Algorithm.HMAC256(KEY));
    }

//    验证token是否正确，即有没有被篡改过或者错误
    public static boolean isValidToken(String token) {
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(KEY)).build();// 生成一个jwt对象
            jwtVerifier.verify(token);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static Map<String,Object> parseTokenToGetUserInfo(String token) {
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

}
