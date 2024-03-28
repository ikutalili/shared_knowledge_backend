package com.yuki.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class SLComment {
    private Boolean like;
    private Boolean dislike;
//    15 in database
    private Integer slCommentId;
    private Integer replyUserId;
    private String replyUserName;

    private String replyAvatar;
    private Integer repliedUserId;
    private String repliedUserName;

    private String repliedAvatar;
    private Integer articleId;
    private String comment;
    private Integer flCommentId;

    private Integer numOfLikes;
    private Integer numOfDislikes;
    private Timestamp commentTime;
}
