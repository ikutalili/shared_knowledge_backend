package com.yuki.service.Impl;

import com.yuki.Utils.RedisUtils;
import com.yuki.entity.CommentGroup;
import com.yuki.entity.Comments;
import com.yuki.entity.FLComment;
import com.yuki.entity.SLComment;
import com.yuki.mapper.FLCommentMapper;
import com.yuki.mapper.SLCommentMapper;
import com.yuki.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {
    private static final String LIKECOMMENT = "likeComment:articleId:";
    private static final String DISLIKECOMMENT = "dislikeComment:articleId:";
    private static final String LIKE_STATUS_OF_COMMENT = "likeStatusOfComment:userId:";
    private static final String DISLIKE_STATUS_OF_COMMENT = "dislikeStatusOfComment:userId:";

    @Autowired
    private FLCommentMapper flCommentMapper;
    @Autowired
    private SLCommentMapper slCommentMapper;
    @Autowired
    private RedisUtils redisUtils;
//    @Override
//    public Map<FLComment, List<SLComment>> getAllCommentsOfArticle(Integer articleId) {
//
////        此处代码将得到一层一层的评论组，每一层都表示第一级评论对象以及其评论下面所有子评论的对象（集合），子评论对象集合
//        Map<FLComment,List<SLComment>> commentGroup = new HashMap<>();
//        List<FLComment> flComments = flCommentMapper.getAllComments(articleId);
//        for (FLComment flComment : flComments) {
//            Integer flCommentId = flComment.getFlCommentId();
//            commentGroup.put(flComment,slCommentMapper.getAllComments(articleId, flCommentId));
//        }
//        return commentGroup;
//    }

//    likeComment:articleId:--flComment:1--10
    public List<CommentGroup> getAllCommentsOfArticle(Integer articleId,String userId) {
//        此处代码将得到一层一层的评论组，每一层都表示第一级评论对象以及其评论下面所有子评论的对象（集合），子评论对象集合
        List<FLComment> flComments = flCommentMapper.getAllComments(articleId);
        List<CommentGroup> commentGroups = new ArrayList<>();
        for (FLComment flComment : flComments) {
            CommentGroup commentGroup = new CommentGroup(); // 评论组
//            设置每条评论的点赞数量以及状态
            flComment.setNumOfLikes(redisUtils.getLikeNumOfComment(LIKECOMMENT + articleId,"flComment:"+flComment.getFlCommentId()));
            flComment.setLike(redisUtils.getLikeStatusOfComment( userId,"flComment:"+flComment.getFlCommentId()));
            flComment.setNumOfDislikes(redisUtils.getDisLikeNumOfComment(DISLIKECOMMENT + articleId,"flComment:"+flComment.getFlCommentId()));
            flComment.setDislike(redisUtils.getDisLikeStatusOfComment( userId,"flComment:"+flComment.getFlCommentId()));
            Integer flCommentId = flComment.getFlCommentId();
            List<SLComment> slComments = slCommentMapper.getAllComments(articleId, flCommentId);
            for (SLComment slComment : slComments) {
                slComment.setNumOfLikes(redisUtils.getLikeNumOfComment(LIKECOMMENT + articleId,"slComment:"+slComment.getSlCommentId()));
                slComment.setLike(redisUtils.getLikeStatusOfComment(  userId,"slComment:"+slComment.getSlCommentId()));
                slComment.setNumOfDislikes(redisUtils.getDisLikeNumOfComment(DISLIKECOMMENT + articleId,"slComment:"+slComment.getSlCommentId()));
                slComment.setDislike(redisUtils.getDisLikeStatusOfComment( userId,"slComment:"+slComment.getSlCommentId()));
            }
            commentGroup.setFlComment(flComment);
            commentGroup.setSlComments(slComments);
            commentGroups.add(commentGroup);
        }
        System.out.println(commentGroups);
        return commentGroups;
    }
    @Override
    public void addFLComment(FLComment flComment) {
        flCommentMapper.addComment(flComment);
    }

    @Override
    public void addSLComment(SLComment slComment) {
        slCommentMapper.addComment(slComment);
    }

//    点赞评论
//    if (redisUtils.getSaveStatus(userId,articleId) && !bool) {
//        redisUtils.operateArticle(articleId,operation,false); // 此操作redis中save数量减一
//        redisUtils.setSaveStatus(userId,articleId,false);    // 并且用户收藏状态为取消收藏
//    }
////        如果还没点过收藏并且接下来要操作的值为true（收藏）的时候，才允许操作，此时操作为自曾
//        else if (!redisUtils.getSaveStatus(userId,articleId) && bool) {
//        redisUtils.operateArticle(articleId,operation,true); // 此操作redis中save数量加一
//        redisUtils.setSaveStatus(userId,articleId,true); // 并且用户收藏状态为开始收藏
//    }
    @Override
    public void likeComment(String articleId,String commentId,String userId,String bool) {
//        开始点赞状态是无的，且即将要点赞（bool为true）的时候执行（+1）
        if (!redisUtils.getLikeStatusOfComment(userId,commentId) && Boolean.parseBoolean(bool)) {
            // commentId已经在controller中设置
            redisUtils.likeComment(articleId,commentId, Boolean.valueOf(bool));
            redisUtils.setLikeStatusOfComment(userId,commentId,Boolean.valueOf(bool));
        }
        else if (redisUtils.getLikeStatusOfComment(userId,commentId) && !Boolean.parseBoolean(bool)) {
            redisUtils.likeComment(articleId,commentId, Boolean.valueOf(bool));
            redisUtils.setLikeStatusOfComment(userId,commentId,Boolean.valueOf(bool));
        }
    }

    @Override
    public void dislikeComment(String articleId, String commentId,String userId, String bool) {
        if (!redisUtils.getDisLikeStatusOfComment(userId,commentId) && Boolean.parseBoolean(bool)) {
            // commentId已经在controller中设置
            redisUtils.dislikeComment(articleId,commentId, Boolean.valueOf(bool));
            redisUtils.setDislikeStatusOfComment(userId,commentId,true);

        }
        else if (redisUtils.getDisLikeStatusOfComment(userId,commentId) && !Boolean.parseBoolean(bool)) {
            redisUtils.dislikeComment(articleId,commentId, Boolean.valueOf(bool));
            redisUtils.setDislikeStatusOfComment(userId,commentId,false);
        }
    }

//    点踩评论


//    添加评论数量至redis
    @Override
    public void addCommentCounts(String articleId) {
        redisUtils.operateArticle(articleId,"comment", true);
    }

    @Override
    public List<Comments> getFLComments() {
        return flCommentMapper.getAllComment();
    }

    @Override
    public List<Comments> getSLComments() {
        return slCommentMapper.getAllComment();
    }

    @Override
    public void deleteFLComment(Integer id) {
        flCommentMapper.deleteCommentById(id);
    }

    @Override
    public void deleteSLComment(Integer id) {
        slCommentMapper.deleteCommentById(id);
    }

    @Override
    public void reportFLComment(String reason, Integer commentId) {
        flCommentMapper.updateReport(reason,commentId);
    }

    @Override
    public void reportSLComment(String reason, Integer commentId) {
        slCommentMapper.updateReport(reason,commentId);
    }

    @Override
    public void deleteCommentOfArticle(Integer id) {
        flCommentMapper.deleteCommentByArticleId(id);
        slCommentMapper.deleteCommentByArticleId(id);
    }
}
