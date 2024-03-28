package com.yuki.entity;

import lombok.Data;

@Data
public class Rating {
    private Integer id;
    private Integer userId;
    private Integer articleId;
    private Integer score;
}
