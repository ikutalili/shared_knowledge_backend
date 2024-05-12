package com.yuki.entity;

import lombok.Data;

import java.util.List;

@Data
public class SocialNetwork {
    private List<User> followings;
    private List<User> fans;
    private List<Article> saves;
    private List<Article> likes;
    private List<Article> write;
}
