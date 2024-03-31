package com.yuki.service.Impl;

import com.yuki.entity.LikeArticle;
import com.yuki.entity.User;
import com.yuki.mapper.ArticleMapper;
import com.yuki.service.LikeArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeArticleServiceImpl implements LikeArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Override
    public void updateLikeArticleStatus(String userId,String userName,String articleId,String operation,Boolean bool) {
        LikeArticle likeArticle = new LikeArticle();
        likeArticle.setStatus(operation);
        likeArticle.setArticleId(articleId);
        likeArticle.setUserId(userId);
        likeArticle.setUserName(userName);
        likeArticle.setTitle(articleMapper.getTitleById(Integer.valueOf(articleId)));
    }
}
