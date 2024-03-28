package com.yuki.mapper;

import com.yuki.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from users where user_id = #{userId}")
    User selectById(Integer userId);
    @Select("select * from users")
    List<User> getAllUsers();
//    @Insert("")
    @Insert("INSERT INTO users (user_name,gender,email,password,registration_time,modify_time)" +
            "VALUES (#{userName},#{gender},#{email},#{password},now(),now() )")
    boolean addUser(String userName,String email, String password,Integer gender);

//    获取用户头像文件名
    @Select("select avatar_url from users where user_id = #{userId}")
    String getAvatarUrlByUserId(Integer userId);

//    给注册的时候，添加载荷时用的
    @Select("select user_id,user_name,email,password from users where email = #{email}")
    User selectUserByEmail(String email);

//    User selectOne(Integer userId);
//    User selectSomeField(int i);

//    更新用户头像
    @Update("update users set " +
            "avatar_url = #{avatar}, modify_time = now() " +
            " where user_id = #{userId}")
    void updateAvatar(Integer userId,String avatar);
//    同步更新文章表的作者头像和名字，如果有的话
    @Update("update articles set " +
            "avatar = #{avatar}, modify_time = now() " +
            " where author_id = #{userId}")
    void updateAuthorAvatar(Integer userId,String avatar);
    @Update("update articles set " +
            "author_name = #{userName}, modify_time = now() " +
            " where author_id = #{userId}")
    void updateAuthorName(Integer userId,String userName);


    //    同步更新评论表的作者头像和名字，如果有的话
    @Update("update first_level_comments set " +
            "avatar = #{avatar}, modify_time = now() " +
            " where user_id = #{userId}")
    void updateFLCommentUserAvatar(Integer userId,String avatar);
    @Update("update first_level_comments set " +
            "user_name = #{userName}, modify_time = now() " +
            " where user_id = #{userId}")
    void updateFLCommentUserName(Integer userId,String userName);

    @Update("update second_level_comments set " +
            "reply_avatar = #{avatar}, modify_time = now() " +
            " where reply_user_id = #{userId}")
    void updateSLCommentUserAvatar(Integer userId,String avatar);
    @Update("update second_level_comments set " +
            "reply_user_name = #{userName}, modify_time = now() " +
            " where reply_user_id = #{userId}")
    void updateSLCommentUserName(Integer userId,String userName);

//    更新用户名
    @Update("update users " +
            "set user_name = #{userName}, " +
            "modify_time = now()" +
            "where user_id = #{userId}"
    )
    void updateUserName(Integer userId,String userName);

//    更新密码
    @Update("update users " +
            "set password = #{password}," +
            "modify_time = now()" +
            "where user_id = #{userId}"
    )
    void updateUserPassword(Integer userId,String password);

    @Update("update users " +
            "set password = #{password}," +
            "modify_time = now()" +
            "where email = #{email}"
    )
    void updateUserPasswordByEmail(String email,String password);
//    检查该邮箱是否已存在
    @Select("select * from users where email = #{email}")
    User selectByEmail(String email);
    User testMultiJoin1();

//    更新用户简介
    @Update("update users set profile = #{profile},modify_time = now() where user_id = #{id}")
    void updateUserProfile(String profile,Integer id);

//    删除/注销用户
    @Delete("delete from users where user_id = #{userId}")
    void deleteUserById(Integer userId);

//    获取用户简介，初始化获取文章信息的时候设置用
    @Select("select profile from users where user_id = #{userId}")
    String getUserProfile(Integer userId);

//    获取所有用户id
    @Select("select user_id from users")
    Integer[] getAllUserId();
}
