package com.yuki.service;

import com.yuki.entity.Article;

import java.util.List;

public interface ArticleService {
    List<Article> getArticlesInfoWithType(String type,String userId);
    List<Article> getAllArticles();

//    添加文章封面
    void addArticleCover(String fileName);

    void saveArticle(Integer id,String name,String avatar,String title,String articleUrl,String type,String preview);

    void operationToArticle(String userId,String articleId,String operation,Boolean bool);

    void reportArticle(String reason,Integer articleId);

    void deleteArticle(Integer id);

    void ratingArticle(String articleId,String userId);

    List<Article> recommendArticlesForUser(Integer userId);
}

