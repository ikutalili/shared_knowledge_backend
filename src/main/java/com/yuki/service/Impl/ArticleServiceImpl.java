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

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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
        if (redisUtils.getSaveStatus(userId,articleId) && !bool && operation.equals("save")) {
            redisUtils.operateArticle(articleId,operation,false); // 此操作redis中save数量减一
            redisUtils.setSaveStatus(userId,articleId,false);    // 并且用户收藏状态为取消收藏
//            if (operation.equals("save")) {
                redisUtils.increment(RATING_PREFIX+userId,articleId,-5);
//            }
        }
//        如果还没点过收藏并且接下来要操作的值为true（收藏）的时候，才允许操作，此时操作为自增
        else if (!redisUtils.getSaveStatus(userId,articleId) && bool && operation.equals("save")) {
            redisUtils.operateArticle(articleId,operation,true); // 此操作redis中save数量加一
            redisUtils.setSaveStatus(userId,articleId,true); // 并且用户收藏状态为开始收藏
            if (!redisUtils.hasRatingKey(userId,articleId)) {
                redisUtils.storeHashData(RATING_PREFIX+userId,articleId,"5");
            }
            else {
                redisUtils.increment(RATING_PREFIX+userId,articleId,5);
            }
        }
        else if (redisUtils.getLikeStatus(userId,articleId) && !bool && operation.equals("like")) {
            redisUtils.operateArticle(articleId,operation,false);
            redisUtils.setLikeStatus(userId,articleId,false);
            redisUtils.increment(RATING_PREFIX+userId,articleId,-3);
        }
        else if (!redisUtils.getLikeStatus(userId,articleId) && bool && operation.equals("like")) {
            redisUtils.operateArticle(articleId,operation,true);
            redisUtils.setLikeStatus(userId,articleId,true);
            if (!redisUtils.hasRatingKey(userId,articleId)) {
                redisUtils.storeHashData(RATING_PREFIX+userId,articleId,"3");
            }
            else {
                redisUtils.increment(RATING_PREFIX+userId,articleId,3);
            }
        }
        else if (redisUtils.getDISLikeStatus(userId,articleId) && !bool && operation.equals("dislike")) {
            redisUtils.operateArticle(articleId,operation,false);
            redisUtils.setDISLikeStatus(userId,articleId,false);
        }
        else if (!redisUtils.getDISLikeStatus(userId,articleId) && bool && operation.equals("dislike")) {
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
            redisUtils.storeHashData(RATING_PREFIX+userId,articleId,"1");
        }
        else {
            redisUtils.increment(RATING_PREFIX+userId,articleId,1);
        }
    }

    @Override
    public List<Article> recommendArticlesForUser(Integer targetUserId) {
        Object hasRated = redisUtils.getAllValuesOfHash(String.valueOf(targetUserId));
        if (hasRated == null) {
            System.out.println("hasRated == null");
        }
//        现在要获取评分矩阵，基于用户协同过滤算法步骤是
//        1.构建评分矩阵  2.通过评分矩阵，计算两两用户的相似度，构建用户相似度矩阵   3.选出top-N物品
        Integer[] allUserId = userMapper.getAllUserId();
        String[] allArticleId = articleMapper.getAllArticleId();
        Integer indexOfTargetUser = null;
        List<Integer> noRatingColumn = new ArrayList<>();
        //        初始化相似度矩阵
        Double[][] similarityMatrix = new Double[allUserId.length][allUserId.length];
//        初始化评分矩阵
        Double[][] ratingMatrix = new Double[allUserId.length][allArticleId.length];
//        构建评分矩阵
        DecimalFormat df = new DecimalFormat("#.#");
        Random rand = new Random();
//        System.out.println("文章长度"+allArticleId.length);
        for (int i = 0; i < allUserId.length; i++) {
            if (Objects.equals(allUserId[i], targetUserId)) {
                indexOfTargetUser = i;
            }
            for (int j = 0; j < allArticleId.length; j++) {
                Double rating = (Double) redisUtils.getHashData(RATING_PREFIX + allUserId[i], allArticleId[j]);
                Double average = redisUtils.getAverageValueOfHash(RATING_PREFIX + allUserId[i]);
                // 如果评分为空，则赋值为平均数
                if (rating == null) {
                    if (Double.isNaN(average)) {
//                        正常情况下NaN代表没有值，用0填充，这里测试用随机数
                        ratingMatrix[i][j] = 1 + rand.nextDouble() * 9;
                    }
                    else {
//                        如果平均值不为空，用平均值填充
                        // 使用DecimalFormat对象来格式化平均值，使其保留一位小数
                        double formattedAverage = Double.parseDouble(df.format(average));
                        ratingMatrix[i][j] = formattedAverage;
                    }
//                    如果用户没有对文章进行评分，则填充初始值，即该用户的评分平均值，因为一开始就让用户
//                    ratingMatrix[i][j] = !Double.isNaN(average) ? average : k++;
//                    并且记录下来,但是要注意记录目标用户的未评分项目，所以要先进行一个判断
                    if (Objects.equals(allUserId[i], targetUserId)) {
                        noRatingColumn.add(j);
                    }
                }
                else {
                    System.out.println("if rating is not null");
                    ratingMatrix[i][j] = rating;
                    System.out.println(Arrays.deepToString(ratingMatrix));
                }
            }
        }
        System.out.println("ratingMatrix");
        System.out.println(Arrays.deepToString(ratingMatrix));
//        根据评分矩阵构建用户相似度矩阵
        for (int i = 0; i < allUserId.length; i++) {
            for (int j = 0; j < allUserId.length; j++) {
                Double similarity = MatrixOperationsUtils.cosineSimilarity(ratingMatrix[i], ratingMatrix[j]);
//                System.out.println("value of similarity "+similarity);
//                System.out.println(Arrays.deepToString(ratingMatrix));
//                如果i == j，说明是用户对应该用户自己
                similarityMatrix[i][j] = (i == j ? 1 : Double.parseDouble(df.format(similarity)));
//                System.out.println("similarityMatrix的值"+similarityMatrix[i][j]);
            }
        }
        System.out.println("similarityMatrix");
        System.out.println(Arrays.deepToString(similarityMatrix));

//        用户相似度矩阵 X 评分矩阵 = 推荐列表
        Double[][] recommendMatrix = MatrixOperationsUtils.multiplyMatrices(similarityMatrix, ratingMatrix);
        System.out.println("已计算好的推荐列表");
        System.out.println(Arrays.deepToString(recommendMatrix));

        Double[] score = new Double[allUserId.length];
        List<Integer> index = new ArrayList<>();
        for (int i = 0,j = 0; i < allUserId.length; i++) {
//            j < allArticleId.length;
            List<Double> aList = new ArrayList<>((Arrays.asList(recommendMatrix[i])));
            Double max = Collections.max(aList);
            int i1 = aList.indexOf(max);
            score[i] = max;
            index.add(i1);
        }
        System.out.println("推荐列表矩阵每行最大的数值");
        System.out.println(Arrays.toString(score));
        System.out.println("最大值对应的下标");
        System.out.println(index);
        System.out.println("allUserId");
        System.out.println(Arrays.toString(allUserId));
        System.out.println("目标用户的下标---"+indexOfTargetUser);
        System.out.println("目标用户对应的那一行");
        System.out.println(Arrays.toString(recommendMatrix[indexOfTargetUser]));

        Double[] recommendList = recommendMatrix[indexOfTargetUser];
        List<Map.Entry<Double, Integer>> scoreIndexList = new ArrayList<>();
        for (Integer indexOfNoRating : noRatingColumn) {
            scoreIndexList.add(new AbstractMap.SimpleEntry<>(recommendList[indexOfNoRating],indexOfNoRating));
        }
        // 按照评分进行降序排序
        scoreIndexList.sort((entry1, entry2) -> Double.compare(entry2.getKey(), entry1.getKey()));

        // 获取前十个元素的下标
        List<Integer> top10Index = new ArrayList<>();
//        Math.min(3, scoreIndexList.size()); 中的 3 代表获取最大的元素
        for (int i = 0; i < Math.min(3, scoreIndexList.size()); i++) {
            top10Index.add(scoreIndexList.get(i).getValue());
        }

        System.out.println("前十个最大元素的下标是: " + top10Index);

//        根据最大元素（最应该推荐的文章）的下标，获取对应文章的id进行推荐
        System.out.println("所有文章id数组");
        System.out.println(Arrays.toString(allArticleId));
        for (Integer eachIndex : top10Index) {
            System.out.println("对应的文章id--"+allArticleId[eachIndex]);
        }
        /*
        * Double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        List<Double> list = new ArrayList<>(Arrays.asList(array));
        System.out.println("转换后的ArrayList: " + list);
        * List<Double> list = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        double maxVal = Collections.max(list);
        int maxIndex = list.indexOf(maxVal);
        * */
//        选出10个邻居，并记录它们的下标，然而这不是必须的
//        List<Integer> topTenIndices = null;
//        if (indexOfTargetUser != null) {
//            List<Double> similarityRow = new ArrayList<>(Arrays.asList(similarityMatrix[indexOfTargetUser]));
//            List<Integer> indices = IntStream.range(0, similarityRow.size()).boxed().sorted((a, b) -> similarityRow.get(b).compareTo(similarityRow.get(a))).collect(Collectors.toList());
//            topTenIndices = indices.subList(0, Math.min(10, indices.size()));
//            System.out.println("top-indices");
//            System.out.println(topTenIndices);
//        }

//        相似度矩阵 X 评分矩阵 = 推荐列表 ??
//        当我们说 “相似度矩阵乘以评分矩阵”（即 S × R），我们的意思是用每个用户的相似度分数来加权其他用户的评分，
//        以此来预测目标用户可能会给项目的评分。

//        根据记录的索引，新建立一个包含目标用户与他邻居的相似度矩阵和评分矩阵
//        try {
////            assert topTenIndices != null;
//            Double[][] newSimilarityMatrix = new Double[topTenIndices.size()+1][];
//            newSimilarityMatrix[0] = similarityMatrix[indexOfTargetUser];
////            topTenIndices
//        }catch (NullPointerException e) {
//            e.printStackTrace();
//        }

        return null;
    }
}
