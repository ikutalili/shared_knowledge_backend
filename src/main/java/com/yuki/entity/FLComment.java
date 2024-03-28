package com.yuki.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class FLComment {
    private Boolean like;
    private Boolean dislike;
    private Integer flCommentId;
    private Integer userId;
    private String userName;
    private String avatar;
    private Integer articleId;
    private String comment;
    private Integer numOfLikes;
    private Integer numOfDislikes;
    private Timestamp commentTime;
}
