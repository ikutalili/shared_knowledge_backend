package com.yuki;

import com.yuki.Utils.RedisUtils;
import com.yuki.entity.Article;
import com.yuki.entity.SocialNetwork;
import com.yuki.mapper.ArticleMapper;
import com.yuki.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class UserTest {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private UserService userService;
    @Test
    void testUserSocialNetworkData() {
//        Map<Object, Object> fans = redisUtils.getAllHashData("fans:50");
//        Map<Object, Object> followings = redisUtils.getAllHashData("following:60");
        Map<Object, Object> likes = redisUtils.getAllHashData("like:user:50");
        Map<Object, Object> saves = redisUtils.getAllHashData("save:user:50");

//        System.out.println(fans);
//        System.out.println(followings);
//        System.out.println(likes);
//        System.out.println(saves);

        for (Object key : likes.keySet()) {
            String o = (String) likes.get(key);
            if (o.equals("true")) {
//                Article saveArticles = articleMapper.getArticleById((Integer.parseInt((String) key)));
                Article saveArticles = articleMapper.getArticleById(6);
                System.out.println(saveArticles.toString());
//                System.out.println(o);
            }
        }


//        Object allValuesOfHash = redisUtils.getAllValuesOfHash("fans:41");
//        System.out.println(allHashData);
//        System.out.println("------------");//{50=true, 59=true}
//        System.out.println(allValuesOfHash);//[true, true]
    }

    @Test
    void testCaseTwo() {
        SocialNetwork socialNetwork;
        socialNetwork = userService.getUserRelationShip("60");
//        System.out.println(socialNetwork.toString());
        System.out.println("------fans------");
        System.out.println(socialNetwork.getFans());
        System.out.println("-------followings");
        System.out.println(socialNetwork.getFollowings());
        System.out.println("------saves-------");
        System.out.println(socialNetwork.getSaves());
        System.out.println("---------likes--------");
        System.out.println(socialNetwork.getLikes());
    }
}
