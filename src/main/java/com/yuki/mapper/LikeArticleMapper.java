package com.yuki.mapper;

import com.yuki.entity.LikeArticle;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeArticleMapper {
    @Insert("insert into like_article (user_id,user_name,article_id,title,status,create_time,modify_time) " +
            "values (#{userId},#{userName},#{articleId},#{title},#{status},now(),now())" +
            "on duplicate key update status = #{status},modify_time = now()")
    void likeArticle(LikeArticle likeArticle);
}
