package com.yuki.entity;

import lombok.Data;

@Data
public class Fans {
    private Integer userId;
    private String userName;
    private Integer fansId;
    private String fansName;
    private Integer followingCounts;
    private Integer fansCounts;
}
