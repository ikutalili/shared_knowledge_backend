package com.yuki.mapper;

import com.yuki.entity.Article;
import lombok.experimental.Delegate;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {
    List<Article> getArticlesOfRecommendation();

    @Select("select * from articles")
    List<Article> getAllArticles();
//    根据传入的类型确定要得到哪个分类下的文章信息
    List<Article> getArticlesWithType(String type);

//    现在是按需查询，因为有一部分字段从redis中获取，不需要查mysql
    @Select("select article_id, author_id,author_name,avatar,article_title,article_url,article_type," +
            "article_cover_url,article_images,publish_time from articles where article_id = #{articleId}")
    Article getArticleById(Integer articleId);
//    存放文章图片到数据库
    @Update("UPDATE articles SET " +
            "article_cover_url = #{fileName},modify_time = now() WHERE " +
            "article_id = #{index}")
    void addImageById(String fileName,Integer index);

//    加文章封面用的，因为发布的时候先创建了文章记录，但又因为封面与文章数据不是同步更新，所以要取最后的index取最新的
    @Select("SELECT article_id from articles ORDER BY article_id DESC LIMIT 1 ")
    Integer getLastIndex();
//      保存文章名字（当作url）
    @Insert("INSERT INTO articles (author_id,author_name,avatar,article_title,preview,article_url,article_type,publish_time,modify_time) VALUES" +
            "( #{id},#{name},#{avatar},#{title},#{preview},#{articleUrl},#{type},now(),now() )")
    void  insertArticle(Integer id,String name,String avatar,String title,String articleUrl,String type,String preview);

//    根据用户id存放他所写的文章的名字
//    @Insert("insert into artiles (article_url) values (#{article}) where author_id = #(authorId)")
//    boolean addArticleById(String avatar,String article,);

//    返回文章的id数组
    @Select("select article_id from articles")
    String [] getAllArticleId();
//更细点赞，点踩，收藏数量
    @Update("update articles set num_of_likes = #{likes},modify_time = now() where article_id = #{articleId}")
    void updateLikesById(Integer articleId,Integer likes);

    @Update("update articles set num_of_dislikes = #{dislikes},modify_time = now() where article_id = #{articleId}")
    void updateDislikesById(Integer articleId,Integer dislikes);

    @Update("update articles set num_of_saves = #{saves},modify_time = now() where article_id = #{articleId}")
    void updateSavesById(Integer articleId,Integer saves);

    @Update("update articles set num_of_comments = #{comments},modify_time = now() where article_id = #{articleId}")
    void updateCommentsById(Integer articleId,Integer comments);

//   举报文章
    @Update("update articles set reported = 1,report_reason = #{reason} where article_id = #{articleId}")
    void reportArticle(String reason,Integer articleId);

//    删除文章
    @Delete("delete from articles where article_id = #{id}")
    void deleteArticle(Integer id);

//    获取所有文章id,以构建评分矩阵,前面已经定义过

//    获取排行榜数据
//    @Select("select * from articles where ")
//    List<Article> getArticlesOfMostLiked()
}
