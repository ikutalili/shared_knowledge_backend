package com.yuki.service;

import com.yuki.entity.CommentGroup;
import com.yuki.entity.Comments;
import com.yuki.entity.FLComment;
import com.yuki.entity.SLComment;

import java.util.List;
import java.util.Map;

public interface CommentService {

    List<CommentGroup> getAllCommentsOfArticle(Integer articleId,String userId);

    List<CommentGroup> getAllCommentsOfArticleWithoutLogin(Integer articleId);
    void addFLComment(FLComment flComment);

    void addSLComment(SLComment slComment);

    void likeComment(String articleId,String commentId,String userId,String bool);

    void dislikeComment(String articleId,String commentId,String userId,String bool);

    void addCommentCounts(String articleId);

    List<Comments> getFLComments();
    List<Comments> getSLComments();

    void deleteFLComment(Integer id);
    void deleteSLComment(Integer id);
    void deleteCommentOfArticle(Integer id);
    void reportFLComment(String reason,Integer commentId);
    void reportSLComment(String reason,Integer commentId);
}
