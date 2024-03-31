package com.yuki.entity;

import lombok.Data;

@Data
public class LikeArticle {
    private String userId;
    private String userName;
    private String articleId;
    private String title;
    private String status;
}
