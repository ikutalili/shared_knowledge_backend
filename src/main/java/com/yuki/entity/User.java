package com.yuki.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class User {
    private Integer userId;
    private String userName;
    private Byte gender;
    private String avatarUrl;
    private String email;
    private String password;
    private String profile;
    private Integer following;
    private Integer followed;
    private String role;
    private Byte reported;
    private Integer numOfArticles;
    private Integer numOfSaves;
    private Timestamp registrationTime;
}

