package com.yuki.service.Impl;

import com.yuki.Utils.MatrixOperationsUtils;
import com.yuki.Utils.RedisUtils;
import com.yuki.entity.Article;
import com.yuki.entity.FLComment;
import com.yuki.mapper.ArticleMapper;
import com.yuki.mapper.FLCommentMapper;
import com.yuki.mapper.SLCommentMapper;
import com.yuki.mapper.UserMapper;
import com.yuki.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private FLCommentMapper flCommentMapper;
    @Autowired
    private SLCommentMapper slCommentMapper;
    private static final String RATING_PREFIX = "rating:";

    @Override
    public List<Article> getArticlesInfoWithType(String type,String userId) {
        List<Article> articles = articleMapper.getArticlesWithType(type);
        for (Article article : articles) {
//            查询，即获取数据只对redis操作
//            此处设置点赞状态，非点赞数量,存取都对redis进行操作，而后再将redis数据同步到mysql，此处应是对点赞状态的 取 ，是初始化加载的状态
//            这三个mysql中没有的字段，从来不向mysql进行读操作，只进行存放数据
            article.setLike(redisUtils.getLikeStatus(userId,article.getArticleId()));
            article.setDislike(redisUtils.getDISLikeStatus(userId,article.getArticleId()));
            article.setSave(redisUtils.getSaveStatus(userId,article.getArticleId()));
            article.setNumOfLikes(redisUtils.getNumOfLikes(article.getArticleId()));
            article.setNumOfDislikes(redisUtils.getNumOfDisikes(article.getArticleId()));
            article.setNumOfSaves(redisUtils.getNumOfSaves(article.getArticleId()));
            article.setNumOfComments(redisUtils.getNumOfComments(article.getArticleId()));
            String hasFollowed = (String) redisUtils.getHashData("following:" + userId, article.getAuthorId());
            article.setHasFollowed(hasFollowed == null ? "false" : "true");
            article.setFollowingCounts(redisUtils.getHashSize("following:"+article.getAuthorId()));
            article.setFansCounts(redisUtils.getHashSize("fans:"+article.getAuthorId()));
            article.setProfile(userMapper.getUserProfile(Integer.valueOf(article.getAuthorId())));
//            每篇文章的评论数量已重新计算赋值，是从两张评论表中分别查询计算出来的，至于数据库中的原有评论数量字段，到时候统一定时从redis中同步
//            article.setNumOfComments(sizeOfAllComments);
        }
        return articles;
    }

    @Override
    public List<Article> getAllArticles() {
        return articleMapper.getAllArticles();
    }

    //    存储数据同时对redis和mysql进行操作
    @Override
    public void operationToArticle(String userId,String articleId, String operation, Boolean bool) {
//        如果已经点过收藏并且接下来要操作的值为false（取消收藏）的时候，才允许操作，此时操作为自减
        if (redisUtils.getSaveStatus(userId,articleId) && !bool) {
            redisUtils.operateArticle(articleId,operation,false); // 此操作redis中save数量减一
            redisUtils.setSaveStatus(userId,articleId,false);    // 并且用户收藏状态为取消收藏
            redisUtils.increment(RATING_PREFIX+userId,articleId,-5L);
        }
//        如果还没点过收藏并且接下来要操作的值为true（收藏）的时候，才允许操作，此时操作为自增
        else if (!redisUtils.getSaveStatus(userId,articleId) && bool) {
            redisUtils.operateArticle(articleId,operation,true); // 此操作redis中save数量加一
            redisUtils.setSaveStatus(userId,articleId,true); // 并且用户收藏状态为开始收藏
            if (!redisUtils.hasRatingKey(userId,articleId)) {
                redisUtils.storeHashData(RATING_PREFIX+userId,articleId,5);
            }
            else {
                redisUtils.increment(RATING_PREFIX+userId,articleId,5L);
            }

        }
        else if (redisUtils.getLikeStatus(userId,articleId) && !bool) {
            redisUtils.operateArticle(articleId,operation,false);
            redisUtils.setLikeStatus(userId,articleId,false);
            redisUtils.increment(RATING_PREFIX+userId,articleId,-3L);
        }
        else if (!redisUtils.getLikeStatus(userId,articleId) && bool) {
            redisUtils.operateArticle(articleId,operation,true);
            redisUtils.setLikeStatus(userId,articleId,true);
            if (!redisUtils.hasRatingKey(userId,articleId)) {
                redisUtils.storeHashData(RATING_PREFIX+userId,articleId,3);
            }
            else {
                redisUtils.increment(RATING_PREFIX+userId,articleId,3L);
            }
        }
        else if (redisUtils.getDISLikeStatus(userId,articleId) && !bool) {
            redisUtils.operateArticle(articleId,operation,false);
            redisUtils.setDISLikeStatus(userId,articleId,false);
        }
        else if (!redisUtils.getDISLikeStatus(userId,articleId) && bool) {
            redisUtils.operateArticle(articleId,operation,true);
            redisUtils.setDISLikeStatus(userId,articleId,true);
        }
    }

    @Override
    public void addArticleCover(String fileName) {
        Integer index = articleMapper.getLastIndex();
        articleMapper.addImageById(fileName,index);
    }

    @Override
    public void saveArticle(Integer id, String name, String avatar, String title, String articleUrl, String type,String preview) {
        articleMapper.insertArticle(id,name,avatar,title,articleUrl,type,preview);
    }

    @Override
    public void reportArticle(String reason,Integer articleId) {
        articleMapper.reportArticle(reason,articleId);
    }

    @Override
    public void deleteArticle(Integer id) {
        articleMapper.deleteArticle(id);
    }

    @Override
    public void ratingArticle(String articleId, String userId) {
        if (!redisUtils.hasRatingKey(userId,articleId)) {
            redisUtils.storeHashData(RATING_PREFIX+userId,articleId,1);
        }
        else {
            redisUtils.increment(RATING_PREFIX+userId,articleId,1L);
        }
    }

    @Override
    public List<Article> recommendArticlesForUser(Integer targetUserId) {
//        现在要获取评分矩阵，基于用户协同过滤算法步骤是
//        1.构建评分矩阵  2.通过评分矩阵，计算两两用户的相似度，构建用户相似度矩阵   3.选出top-N物品
        Integer[] allUserId = userMapper.getAllUserId();
        String[] allArticleId = articleMapper.getAllArticleId();
        int index ;
//        初始化相似度矩阵
        Double[][] similarityMatrix = new Double[allUserId.length][allUserId.length];
//        初始化评分矩阵
        Double[][] ratingMatrix = new Double[allUserId.length][allArticleId.length];
//        构建评分矩阵
        for (int i = 0; i < allUserId.length; i++) {
            if (Objects.equals(allUserId[i], targetUserId)) {
                index = i;
            }
            for (int j = 0; j < allArticleId.length; j++) {
                Double rating = (Double) redisUtils.getHashData(RATING_PREFIX + allUserId[i], allArticleId[j]);
                Double average = redisUtils.getAverageValueOfHash(RATING_PREFIX + allUserId[i]);
                if (rating == null) {
//                    如果用户没有对文章进行评分，则填充初始值，即该用户的评分平均值，因为一开始就让用户
                    ratingMatrix[i][j] = average != null ? average : 0;
                }
                else {
                    ratingMatrix[i][j] = rating;
                }
            }
        }

//        根据评分矩阵构建用户相似度矩阵
        for (int i = 0; i < allUserId.length; i++) {
            for (int j = 0; j < allUserId.length; j++) {
                Double similarity = MatrixOperationsUtils.cosineSimilarity(ratingMatrix[i], ratingMatrix[j]);
//                如果i == j，说明是用户对应该用户自己
                similarityMatrix[i][j] = i == j ? 1 : similarity;
            }
        }

//        相似度矩阵 X 评分矩阵 = 推荐列表 ??



        return null;
    }
}
