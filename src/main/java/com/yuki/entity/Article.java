package com.yuki.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Article {
    private Boolean like;
    private Boolean dislike;
    private Boolean save;
    private String articleId;

    private String authorId;
    private String authorName;
    private String avatar;
    private String profile;

    private String preview;
    private String articleTitle;
    private String articleUrl;
    private String articleType;
    private String articleCoverUrl;
    private String articleImages;
    private Integer numOfLikes;
    private Integer numOfDislikes;
    private Integer numOfSaves;
    private Integer numOfComments;
    private Integer reported;
    private String reportReason;
    private Timestamp publishTime;
    private String hasFollowed;
    private Long followingCounts;
    private Long fansCounts;
}
