package com.yuki.entity;

import lombok.Data;

@Data
public class Similarity {
    private Integer id;
    private Integer user1;
    private Integer user2;
    private Integer similarity;
}
