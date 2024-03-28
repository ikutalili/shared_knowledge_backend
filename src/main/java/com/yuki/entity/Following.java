package com.yuki.entity;

import lombok.Data;

@Data
public class Following {
    private Integer userId;
    private String userName;
    private Integer followingId;
    private String followingName;
    private Integer followingCounts;
    private Integer fansCounts;
}
