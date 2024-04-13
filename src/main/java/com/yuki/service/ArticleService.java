package com.yuki.service;

import com.yuki.entity.Article;

import java.util.List;

public interface ArticleService {
    List<Article> getArticlesInfoWithType(String type,String userId);
    List<Article> getAllArticles();

    List<Article> getArticlesInfoByTypeWithoutLogin(String type);
//    为用户提供排行榜文章，无论是否登录，都提供点赞数前十的文章
    List<Article> getHotArticles(String userId);
    List<Article> getHotArticlesForNoLogin();
//   为未登录 提供推荐文章
    List<Article> recommendArticlesForNoLogin();
//    添加文章封面
    void addArticleCover(String fileName);

    void saveArticle(Integer id,String name,String avatar,String title,String articleUrl,String type,String preview);

    void operationToArticle(String userId,String articleId,String operation,Boolean bool,String userName);

    void reportArticle(String reason,Integer articleId);

    void deleteArticle(Integer id);

    void ratingArticle(String articleId,String userId);

    List<Article> recommendArticlesForUser(Integer userId);


}

