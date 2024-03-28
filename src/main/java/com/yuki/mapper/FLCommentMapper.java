package com.yuki.mapper;

import com.yuki.entity.Comments;
import com.yuki.entity.FLComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FLCommentMapper {
    @Select("select * from first_level_comments where article_id = #{articleId}")
    List<FLComment> getAllComments(Integer articleId);

//    获取每一篇文章的评论数量
    @Select("select count(*) from first_level_comments where article_id = #{articleId}")
    Integer getFLCommentNum(Integer articleId);

    //    新增第一级评论
    void addComment(FLComment flComment);

//    按照id删除评论
    @Delete("delete from first_level_comments where fl_comment_id = #{id}")
    void deleteCommentById(Integer id);

    @Delete("delete from first_level_comments where article_id = #{id}")
    void deleteCommentByArticleId(Integer id);
    /**
     * "select sl_comment_id as comment_id,reply_user_id as user_id,reply_user_name as user_name,comment,num_of_likes," +
     *             "num_of_dislikes,reported,comment_time" +
     *             " from second_level_comments"
     *
     */
//    获取所有评论
    @Select("select fl_comment_id as comment_id,user_id,user_name,comment,num_of_likes,num_of_dislikes,reported,report_reason,comment_time from " +
            "first_level_comments")
    List<Comments> getAllComment();

//    举报评论
    @Update("update first_level_comments set reported = 1,report_reason = #{reason} where fl_comment_id = #{commentId}")
    void updateReport(String reason,Integer commentId);


}
