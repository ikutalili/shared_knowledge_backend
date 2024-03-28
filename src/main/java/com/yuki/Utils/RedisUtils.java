package com.yuki.Utils;

import com.yuki.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    private static final String LIKE = "like:user:";
    private static final String DISLIKE = "dislike:user:";
    private static final String SAVE = "save:user:";
    private static final String LIKECOMMENT = "likeComment:articleId:";
    private static final String DISLIKECOMMENT = "dislikeComment:articleId:";
    private static final String FLCOMMENT = "flComment:";
    private static final String SLCOMMENT = "slComment:";
    private static final String LIKE_STATUS_OF_COMMENT = "likeStatusOfComment:userId:";
    private static final String DISLIKE_STATUS_OF_COMMENT = "dislikeStatusOfComment:userId:";
//    private  StringRedisTemplate stringRedisTemplate;
//    @Autowired
//    public RedisUtils(StringRedisTemplate stringRedisTemplate){
//        this.stringRedisTemplate = stringRedisTemplate;
//    }
//    @Autowired
//    private RedisTemplate<String,String> redisTemplate;
//    @Autowired
//    private ArticleMapper articleMapper;
//    private final HashOperations<String, Object, Object> ops;
//    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
//        this.ops = redisTemplate.opsForHash();
//    }
    private final StringRedisTemplate redisTemplate;
//    private final RedisTemplate<String,String> redisTemplate;
    private final ArticleMapper articleMapper;
    private final HashOperations<String, Object, Object> ops;
    private final ValueOperations<String, String> operations;
    @Autowired
    public RedisUtils(StringRedisTemplate stringRedisTemplate, ArticleMapper articleMapper) {
//        this.stringRedisTemplate = stringRedisTemplate;
//        this.redisTemplate = redisTemplate;
        this.redisTemplate = stringRedisTemplate;
        this.articleMapper = articleMapper;
        this.ops = redisTemplate.opsForHash();
        this.operations = stringRedisTemplate.opsForValue();
    }

    public  void storeValue (String email,String validationCode) {
//        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set(email,validationCode,3, TimeUnit.MINUTES);
    }

    public void storeHashData(String key,Object field,Object value) {
        ops.put(key,field,value);
    }
    public Object getHashData(String key, Object field) {
        return ops.get(key,field);
    }

    public Map<Object,Object> getAllHashData(String hashKey) {
        return ops.entries(hashKey);
    }
    public void removeHashData(String key,Object field) {
        ops.delete(key,field);
    }
//    哈希数据自增
    public void increment(String key,Object hashKey,Long delta) {
        ops.increment(key,hashKey,delta);
    }

    public Long getHashSize(String key) {
        return ops.size(key);
    }
//    验证存储在redis中的验证码是否有效，传入要验证的码，与redis中的做比较
    public boolean validateValue(String email,String value) {
        String valueInRedis = operations.get(email);
        return value.equals(valueInRedis);
    }

//    存储token值

    /**
     *
     * @param key token值
     * @param value token值
     * @param time 时间单位分钟
     */
    public  void storeValueInMinutes (String key,String value,Integer time) {
//        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set(key,value,time, TimeUnit.MINUTES);
    }

    /**
     *
     * @param key token
     * @param value token
     * @param time timeUnit.hours
     */
    public void storeValueInHours (String key,String value,Integer time) {
//        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set(key,value,time, TimeUnit.HOURS);
    }
    public void removeValue(String key) {
        operations.getOperations().delete(key);
    }
    /**
     *
     * @param key token值
     * @return 验证是否相等
     */
    public boolean validateToken(String key) {
//        String valueInRedis = stringRedisTemplate.opsForValue().get(key);
        String valueInRedis = operations.get(key);  // 如果key存在，那么value不为空
        return key.equals(valueInRedis);
    }

//    从redis中获取token
    public String getToken(String key) {
//        return stringRedisTemplate.opsForValue().get(key);
        return operations.get(key);
    }

    public Boolean removeToken(String key) {
        return redisTemplate.delete(key);
    }

    /*  对每篇文章的点赞状态
     *  like:userId--articleId--true
     *             --articleId--false

        对每篇文章的操作                */
//    articleId--like--234
//              --dislike--12

public boolean hasRatingKey(String userId,Object articleId) {
    return ops.hasKey("rating:"+userId,articleId);
}
//    文章点赞,点踩，收藏,example 2,like,true，表示对文章2如果like操作是true，数量+1，反则依然

    /**
     *
     * @param articleId 要操作的文章id
     * @param operation 要进行的操作
     * @param operationBool 操作的值(true or false)
     */
//    此方法用于记录对一篇文章所有操作（点赞，踩，收藏，评论）的数量的自增自减,增减都相当于 set 操作，对应的get操作在下面
//    注意，redis初始化数据需要与mysql保持一致
    public void operateArticle(String articleId,String operation,Boolean operationBool) {
//        如果该键不存在，那么创建新的，此方法可操作对文章的的点赞数量
        if (!ops.hasKey("operateToArticle:" + articleId,operation)) {
            ops.put("operateToArticle:" + articleId,operation,"1");
        }
        else {
//            如果键是存在的，那么根据true+1，false-1
            if (operationBool) {
                ops.increment("operateToArticle:" + articleId,operation,1);
            }
            else {
                ops.increment("operateToArticle:" + articleId,operation,-1);
            }
        }
    }
    private void handleComment(String keyPrefix, String articleId, String commentId, Boolean bool) {
        String key = keyPrefix + articleId; // like:articleId:1 -- flComment:1--true
        if (!ops.hasKey(key, commentId)) {  // 最开始如果这条评论没有被记录，就加一点赞数
            ops.put(key, commentId, "1");
        } else {
            if (bool) {
                ops.increment(key, commentId, 1);
            } else {
                ops.increment(key, commentId, -1);
            }
        }
    }
//    LIKECOMMENT = "like:articleId:"; 每当点赞了一条评论，就往redis中+1 likeComment:articleId:--flComment:1--10
//    set 不需要输入前缀，只要输入文章id，但评论id要前缀 get 需要前缀
    public void likeComment(String articleId,String commentId,Boolean bool) {
        handleComment(LIKECOMMENT, articleId, commentId, bool);
}
//     DISLIKECOMMENT = "dislike:articleId:"
    public void dislikeComment(String articleId,String commentId,Boolean bool) {
        handleComment(DISLIKECOMMENT, articleId, commentId, bool);
    }
//    将会得到某条评论的点赞数,注意commentId 必须为 -- flComment/slComment
    public Integer getLikeNumOfComment(String articleId,String commentId) {
        Object likes = ops.get(articleId, commentId);
        if (likes != null) {
            return Integer.parseInt((String)likes) ;
        }
        else {
            return 0;
        }
    }
    public Integer getDisLikeNumOfComment(String articleId,String commentId) {
        Object dislikes = ops.get(articleId, commentId);
        if (dislikes != null) {
            return Integer.parseInt((String)dislikes) ;
        }
        else {
            return 0;
        }
    }


    // ------对评论也要记录一个点赞，点踩的状态，要注意与文章的点赞区分--------此数据结构只需要知道 userId，commentId,true就能定位评论
//    likeStatusOfComment:userId:1--flCommentId:12--true
    public void setLikeStatusOfComment(String userId,String commentId,Boolean bool) {
        ops.put(LIKE_STATUS_OF_COMMENT + userId,commentId, Boolean.toString(bool));
    }
    public Boolean getLikeStatusOfComment(String userId,String commentId) {
        Object value = ops.get(LIKE_STATUS_OF_COMMENT + userId, commentId);
        return value != null && Boolean.parseBoolean((String) value);
    }
    public void setDislikeStatusOfComment(String userId,String commentId,Boolean bool) {
        ops.put(DISLIKE_STATUS_OF_COMMENT + userId,commentId, Boolean.toString(bool));
    }
    public Boolean getDisLikeStatusOfComment(String userId,String commentId) {
        Object value = ops.get(DISLIKE_STATUS_OF_COMMENT + userId, commentId);
        return value != null && Boolean.parseBoolean((String) value);
    }
//    ------------------------------
    public Map<Object,Object> getAllByArticleId(String articleId) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        return ops.entries("operateToArticle:" + articleId);
    }

//    返回点赞数量，下面依次是点踩，收藏
    public Integer getNumOfLikes(String articleId) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        String likesStr = (String) ops.get("operateToArticle:" + articleId, "like");
        if (likesStr != null) {
            return Integer.parseInt(likesStr);
        } else {
            return 0; // 如果没有点赞记录，则返回默认值
        }
    }
    public Integer getNumOfDisikes(String articleId) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        String dislikesStr = (String) ops.get("operateToArticle:" + articleId, "dislike");
        if (dislikesStr != null) {
            return Integer.parseInt(dislikesStr);
        } else {
            return 0; // 如果没有点踩记录，则返回默认值
        }
    }
    public Integer getNumOfSaves(String articleId) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        String savesStr = (String) ops.get("operateToArticle:" + articleId, "save");
        if (savesStr != null) {
            return Integer.parseInt(savesStr);
        } else {
            return 0; // 如果没有保存记录，则返回默认值
        }
    }
    public Integer getNumOfComments(String articleId) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        String commentsStr = (String) ops.get("operateToArticle:" + articleId, "comment");
        if (commentsStr != null) {
            return Integer.parseInt(commentsStr);
        } else {
            return 0; // 如果没有点赞记录，则返回默认值
        }
    }

//    记录用户的点赞状态,key为 like：user：1--1--true，意思是用户1对文章1点赞操作为true
    public void setLikeStatus(String userId,String articleId,Boolean operationBool) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
//        ops.put(LIKE + userId,articleId,operationBool ? "true" : "false");
        ops.put(LIKE + userId,articleId,Boolean.toString(operationBool));
    }
    public Boolean getLikeStatus(String userId,String articleId) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        Object value = ops.get(LIKE + userId, articleId);
        return value != null && Boolean.parseBoolean((String) value);
    }

//    记录用户的点踩状态
    public void setDISLikeStatus(String userId,String articleId,Boolean operationBool) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        ops.put(DISLIKE + userId,articleId,Boolean.toString(operationBool));
}
    public Boolean getDISLikeStatus(String userId,String articleId) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        Object value = ops.get(DISLIKE + userId, articleId);
        return value != null && Boolean.parseBoolean((String) value);
//        return ops.get(DISLIKE + userId,articleId) == null ? "false" : "true";
    }

//    记录用户的保存状态
    public void setSaveStatus(String userId,String articleId,Boolean operationBool) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        ops.put(SAVE + userId,articleId,Boolean.toString(operationBool));
}
    public Boolean getSaveStatus(String userId,String articleId) {
//        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        Object value = ops.get(SAVE + userId, articleId);
        return value != null && Boolean.parseBoolean((String) value);
    }
}
