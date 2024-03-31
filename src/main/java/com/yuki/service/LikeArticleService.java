package com.yuki.service;

import com.yuki.entity.User;

public interface LikeArticleService {
    void updateLikeArticleStatus(String id,String name,String articleId,String operation,Boolean bool);
}
