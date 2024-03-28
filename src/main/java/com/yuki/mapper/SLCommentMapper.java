package com.yuki.mapper;

import com.yuki.entity.Comments;
import com.yuki.entity.SLComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SLCommentMapper {

    @Select("select * from second_level_comments where article_id = #{articleId} and fl_comment_id = #{flCommentId}")
    List<SLComment> getAllComments(Integer articleId,Integer flCommentId);

    @Select("select count(*) from second_level_comments where article_id = #{articleId} and fl_comment_id = #{flCommentId}")
    Integer getSLCommentNum(Integer articleId,Integer flCommentId);

//    新增第二级评论
    void addComment(SLComment slComment);

//    通过评论id删除评论
    @Delete("delete from second_level_comments where sl_comment_id = #{id}")
    void deleteCommentById(Integer id);

//    删除某篇文章所属的所有评论
    @Delete("delete from second_level_comments where article_id = #{id}")
    void deleteCommentByArticleId(Integer id);
    @Select("select sl_comment_id as comment_id,reply_user_id as user_id,reply_user_name as user_name,comment,num_of_likes," +
            "num_of_dislikes,reported,report_reason,comment_time" +
            " from second_level_comments")
    List<Comments> getAllComment();

//    举报评论
    @Update("update second_level_comments set reported = 1,report_reason = #{reason} where sl_comment_id = #{commentId}")
    void updateReport(String reason,Integer commentId);
}
