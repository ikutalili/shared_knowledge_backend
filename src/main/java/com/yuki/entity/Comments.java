package com.yuki.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Comments {
    private Integer commentId;
    private Integer userId;
    private String userName;
    private String comment;
    private Integer numOfLikes;
    private Integer numOfDislikes;
    private Integer reported;
    private String reportReason;
    private Timestamp commentTime;
}
