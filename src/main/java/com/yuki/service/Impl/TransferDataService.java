package com.yuki.service.Impl;

import com.yuki.Utils.RedisUtils;
import com.yuki.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransferDataService {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ArticleMapper articleMapper;

//    @Scheduled(fixedRate = 5000)
//    @Transactional
    public void transferData() {
        String[] allArticleId = articleMapper.getAllArticleId();
        for (String articleId : allArticleId) {
//            分别向mysql数据库同步更新点赞，收藏等的数量，只要redis数据库中有的，都同步到mysql中去，没有的默认0，同步到mysql中去
//            此处点赞状态等不需要进行同步，因为mysql数据库中并没有相应字段
            articleMapper.updateLikesById(Integer.parseInt(articleId),redisUtils.getNumOfLikes(articleId));
            articleMapper.updateDislikesById(Integer.parseInt(articleId),redisUtils.getNumOfDisikes(articleId));
            articleMapper.updateSavesById(Integer.parseInt(articleId),redisUtils.getNumOfSaves(articleId));
            articleMapper.updateCommentsById(Integer.parseInt(articleId),redisUtils.getNumOfComments(articleId));
        }
    }

}
