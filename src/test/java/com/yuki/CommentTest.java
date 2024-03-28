package com.yuki;

import com.yuki.Utils.RedisUtils;
import com.yuki.entity.FLComment;
import com.yuki.entity.SLComment;
import com.yuki.mapper.ArticleMapper;
import com.yuki.mapper.FLCommentMapper;
import com.yuki.mapper.SLCommentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class CommentTest {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SLCommentMapper slCommentMapper;
    @Autowired
    private FLCommentMapper flCommentMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Test
    void getSLComment() {
        List<SLComment> allComments = slCommentMapper.getAllComments(1, 1);
        System.out.println(allComments);

    }

    @Test
    void testOperation() {
        redisUtils.operateArticle("1","like",false);
        redisUtils.operateArticle("1","save",false);
        redisUtils.operateArticle("1","dislike",true);
        Map<Object, Object> info = redisUtils.getAllByArticleId("1");
        System.out.println(info);
    }
    @Test
    void testGetArticleData() {
        Map<Object, Object> info = redisUtils.getAllByArticleId("1");
//        System.out.println("like->"+info.get("like"));
//        System.out.println("save->"+info.get("save"));
//        System.out.println("dislike"+info.get("dislike"));
//        System.out.println(info);
//        System.out.println(redisUtils.getNumOfLikes("1"));
//        redisUtils.setLikeStatus("1","1",true);
//        String status = redisUtils.getLikeStatus("1", "1");
//        System.out.println("------"+status);
    }
    @Test
    void transferDataTest() {
//        String[] allArticleId = articleMapper.getAllArticleId();
//        System.out.println(Arrays.toString(allArticleId));
//        articleMapper.updateLikesById(1,16);

//        redisUtils.setSaveStatus("1","1",true);
//        redisUtils.operateArticle("1","like",true);
//        redisUtils.setDisikeStatusOfComment("1","1",true);
    }
}
