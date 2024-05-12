package com.yuki.service.Impl;

import com.yuki.Utils.CaptchasUtils;
import com.yuki.Utils.JwtUtils;
import com.yuki.Utils.PasswordUtils;
import com.yuki.Utils.RedisUtils;
import com.yuki.entity.Article;
import com.yuki.entity.SocialNetwork;
import com.yuki.entity.User;
import com.yuki.mapper.ArticleMapper;
import com.yuki.mapper.UserMapper;
import com.yuki.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CaptchasUtils captchasUtils;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ArticleMapper articleMapper;
    private static final String PASSWORD_LENGTH_PATTERN = "^.{8,16}$";
    private static final String USERNAME_LENGTH_PATTERN = "^.{1,100}$";
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

//    @Override
//    public User getUserInfoByEmail(String email) {
//        return userMapper.selectUserByEmail(email);
//    }

    @Override
    public User getUserInfoById(Integer userId) {
        return userMapper.selectById(userId);
    }

//    通过token解析出userid，然后再通过id查询出这个用户所有信息
    @Override
    public User getUserInfoByToken(String token) {
        Map<String, Object> userInfo = JwtUtils.parseTokenToGetUserInfo(token);
        Object userId = userInfo.get("userId");
        User user = userMapper.selectById((Integer) userId);
        Integer[] numOfArticles = userMapper.getNumOfArticlesByUserId((Integer) userId);
        user.setNumOfArticles(numOfArticles.length);
        return user;
    }

    //    用户注册
    @Override
    public String addUser(String userName,String email,String password,Integer gender,String Captcha) {

        if (!userName.isEmpty() && !email.isEmpty() && !password.isEmpty() && !Captcha.isEmpty()) {
            //        判断收到的数据是否合法
            boolean userNameBool = userName.matches(USERNAME_LENGTH_PATTERN);
            boolean passwordBool = password.matches(PASSWORD_LENGTH_PATTERN);
//        判断邮箱格式是否符合要求
            boolean emailBool = email.matches(EMAIL_PATTERN);

            boolean codeIsEffective = captchasUtils.validateCode(email,Captcha);

//        如果所有数据都没问题，都是有效的，那么允许注册
            if (userNameBool && passwordBool && emailBool && codeIsEffective) {

                try {
                    password = PasswordUtils.passwordEncrypt(password);
                    userMapper.addUser(userName,email,password,gender);
                    User user = userMapper.selectByEmail(email);

                    Map<String, Object> claims = new HashMap<>();
                    claims.put("userId",user.getUserId());
                    claims.put("name",user.getUserName());
                    String token = JwtUtils.genToken(claims,72);
                    ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
//            set time
                    operations.set(token,token,72, TimeUnit.HOURS);
                    captchasUtils.removeCode(email);
                    return token;
                }catch (Exception e){
                    e.printStackTrace();
//                System.out.println("emailInUsed");
                    return "emailInUsed";
                }
            }
            else {
//                此处如果数据格式不对，例如邮箱格式，则返回数据错误
                return "dataError";
            }
        }
        else {
//            如果有收到数据是空的，那么返回数据错误
            return "dataError";
        }
    }

//    用户登录判断
    @Override
    public String userLogin(String email, String password) {
        if (email != null){
            try {
//                此处如果邮箱不存在，则查询结果为空，抛出异常
                User user = userMapper.selectUserByEmail(email);
                //        比较接收的密码和是否与数据库的相同
                String passwordInDB = user.getPassword();
//                声明载荷，用于准备签发token
                String userName = user.getUserName();
                Integer userId = user.getUserId();
//                检查密码是否与加密后的一样
                if ( PasswordUtils.isMatch(password, passwordInDB) ) {
//          jwt载荷由 用户id 和 用户名组成
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("userId", userId);
                    claims.put("name", userName);
                    String token = JwtUtils.genToken(claims, 24);
//            同时把token写到redis数据库中
                    ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
//            set time
                    operations.set(token, token, 24, TimeUnit.HOURS);
                    System.out.println("--------login-->" + token);
                    return token;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void updateUserAvatar(Integer userId, String avatarUrl) {
        try {
            userMapper.updateAvatar(userId, avatarUrl);//更新用户头像
            userMapper.updateAuthorAvatar(userId,avatarUrl);//关联的作者头像
            userMapper.updateFLCommentUserAvatar(userId,avatarUrl);//关联的评论头像
            userMapper.updateSLCommentUserAvatar(userId,avatarUrl);
//            return true;
        }
        catch (Exception e) {
//            return false;
        }
    }

    @Override
    public User editUserInfo(Integer userId, String userName, String password,String captcha,String oldToken,String profile) {
        User user = userMapper.selectById(userId);
        String email = user.getEmail();
        if (userName != null && !userName.isEmpty()) {
            try {
                userMapper.updateUserName(userId, userName);
                userMapper.updateAuthorName(userId,userName);
                userMapper.updateUserProfile(profile,userId);
                userMapper.updateFLCommentUserName(userId,userName);
                userMapper.updateSLCommentUserName(userId,userName);
            }catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
//        如果密码和验证码都是有效的，允许修改，原token作废
        if ( (password != null && !password.isEmpty() ) && ( captcha != null && !captcha.isEmpty() ) && captchasUtils.validateCode(email,captcha)) {
            try {
//                验证码验证不成功，token没有失效
                System.out.println("验证码是否有效"+captchasUtils.validateCode(email,captcha));
                System.out.println("oldToken----"+oldToken);
//                先加密，再保存密码
                userMapper.updateUserPassword(userId, PasswordUtils.passwordEncrypt(password));

//                删除旧token
                Boolean aBoolean = redisUtils.removeToken(oldToken);
                System.out.println(aBoolean);
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
//        else {
//            System.out.println(password);
//            System.out.println(captcha);
//            System.out.println("密码或验证码验证不通过");
//            return null;
//        }
        return userMapper.selectById(userId);
    }

    @Override
    public String getAvatarFileName(Integer userId) {
        return  userMapper.getAvatarUrlByUserId(userId);
    }

    @Override
    public User checkEmailExists(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Override
    public void followUser(String userId, String followingId) {
//        存放到redis中去，定时写到数据库中
        if (!followingId.isEmpty() && !userId.isEmpty()) {
            redisUtils.storeHashData("following:"+userId,followingId,"true");
            redisUtils.storeHashData("fans:"+followingId,userId,"true");
        }
    }

    @Override
    public void unfollowUser(String userId, String followingId) {
        if (!followingId.isEmpty() && !userId.isEmpty()) {
            redisUtils.removeHashData("following:"+userId,followingId);
            redisUtils.removeHashData("fans:"+followingId,userId);
        }
    }

    @Override
    public void deleteUser(Integer id) {
        userMapper.deleteUserById(id);
    }

    @Override
    public Integer[] getAllUserId() {
        return userMapper.getAllUserId();
    }

//    Map<Object, Object> fans = redisUtils.getAllHashData("fans:60");
//    Map<Object, Object> followings = redisUtils.getAllHashData("following:60");
//    Map<Object, Object> likes = redisUtils.getAllHashData("like:user:60");
//    Map<Object, Object> saves = redisUtils.getAllHashData("save:user:60");
    public List<Article> addStatusForArticle(List<Article> articles,String userId) {
        for (Article article : articles) {
//            查询，即获取数据只对redis操作
//            此处设置点赞状态，非点赞数量,存取都对redis进行操作，而后再将redis数据同步到mysql，此处应是对点赞状态的 取 ，是初始化加载的状态
//            这三个mysql中没有的字段，从来不向mysql进行读操作，只进行存放数据
            if (article != null) {
                article.setLike(redisUtils.getLikeStatus(userId,article.getArticleId()));
                article.setDislike(redisUtils.getDISLikeStatus(userId,article.getArticleId()));
                article.setSave(redisUtils.getSaveStatus(userId,article.getArticleId()));
//            这些数据虽然mysql中有，但是优先从redis中获取
                article.setNumOfLikes(redisUtils.getNumOfLikes(article.getArticleId()));
                article.setNumOfDislikes(redisUtils.getNumOfDislikes(article.getArticleId()));
                article.setNumOfSaves(redisUtils.getNumOfSaves(article.getArticleId()));
                article.setNumOfComments(redisUtils.getNumOfComments(article.getArticleId()));
                String hasFollowed = (String) redisUtils.getHashData("following:" + userId, article.getAuthorId());
                article.setHasFollowed(hasFollowed == null ? "false" : "true");
                article.setFollowingCounts(redisUtils.getHashSize("following:"+article.getAuthorId()));
                article.setFansCounts(redisUtils.getHashSize("fans:"+article.getAuthorId()));
                article.setProfile(userMapper.getUserProfile(Integer.valueOf(article.getAuthorId())));
            }
//            每篇文章的评论数量已重新计算赋值，是从两张评论表中分别查询计算出来的，至于数据库中的原有评论数量字段，到时候统一定时从redis中同步
//            article.setNumOfComments(sizeOfAllComments);
        }
        return articles;
    }
    @Override
    public SocialNetwork getUserRelationShip(String userId) {
        SocialNetwork userSocialNetwork = new SocialNetwork();
        Map<Object, Object> fans = redisUtils.getAllHashData("fans:" + userId);
        Map<Object, Object> followingsMap = redisUtils.getAllHashData("following:" + userId);
        Map<Object, Object> likesMap = redisUtils.getAllHashData("like:user:" + userId);
        Map<Object, Object> savesMap = redisUtils.getAllHashData("save:user:" + userId);
        List<User> Fans = new ArrayList<>();
        List<User> followings = new ArrayList<>();
        List<Article> saves = new ArrayList<>();
        List<Article> likes = new ArrayList<>();
        List<Article> write;
        for (Object key : fans.keySet()) {
            String o = (String) fans.get(key);
            if ("true".equals(o)) {
                User user = userMapper.selectById((Integer.parseInt((String) key)));
                Fans.add(user);
            }
        }
        for (Object key : followingsMap.keySet()) {
            String o = (String) followingsMap.get(key);
            if ("true".equals(o)) {
                User user = userMapper.selectById((Integer.parseInt((String) key)));
                followings.add(user);
            }
        }
        for (Object key : savesMap.keySet()) {
            String o = (String) savesMap.get(key);
            if ("true".equals(o)) {
                Article saveArticles = articleMapper.getArticleById((Integer.parseInt((String) key)));
                saves.add(saveArticles);
            }
        }
        for (Object key : likesMap.keySet()) {
            String o = (String) likesMap.get(key);
            if ("true".equals(o)) {
                Article likeArticles = articleMapper.getArticleById((Integer.parseInt((String) key)));
                likes.add(likeArticles);
            }
        }
        write = articleMapper.getArticlesByUserId(Integer.valueOf(userId));
        try {
            userSocialNetwork.setWrite(addStatusForArticle(write,userId));
            userSocialNetwork.setFans(Fans);
            userSocialNetwork.setFollowings(followings);
            userSocialNetwork.setSaves(addStatusForArticle(saves,userId));
            userSocialNetwork.setLikes(addStatusForArticle(likes,userId));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return userSocialNetwork;
    }
}
