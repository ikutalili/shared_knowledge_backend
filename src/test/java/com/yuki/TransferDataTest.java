package com.yuki;

import com.yuki.entity.LikeArticle;
import com.yuki.mapper.LikeArticleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TransferDataTest {
    @Autowired
    private LikeArticleMapper likeArticleMapper;
    @Test
    void test1() {
        LikeArticle likeArticle = new LikeArticle();
        likeArticle.setArticleId("1");
        likeArticle.setTitle("yes");
        likeArticle.setStatus("true");
        likeArticleMapper.likeArticle(likeArticle);
    }
}
