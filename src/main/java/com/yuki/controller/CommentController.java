package com.yuki.controller;

import com.yuki.Utils.RedisUtils;
import com.yuki.entity.*;
import com.yuki.mapper.SLCommentMapper;
import com.yuki.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CommentController {
    private CommentService commentService;
    private RedisUtils redisUtils;
    @Autowired
    public CommentController(CommentService commentService, RedisUtils redisUtils) {
        this.commentService = commentService;
        this.redisUtils = redisUtils;
    }
    @PostMapping("/flComment/{articleId}")
    Result<Void> addFLComment(@RequestBody FLComment flComment,@PathVariable String articleId,HttpServletRequest request){
        User user = (User) request.getAttribute("user");
        if (isLogin(request)) {
            if (flComment.getComment() != null) {
                try {
                    flComment.setUserId(user.getUserId());
                    flComment.setUserName(user.getUserName());
                    flComment.setAvatar(user.getAvatarUrl());
                    commentService.addFLComment(flComment);
                    commentService.addCommentCounts(articleId);
                    return Result.successWithoutData();
                }catch (Exception e) {
                    e.printStackTrace();
                    return Result.error();
                }
            }else {
                return new Result<>(1,"comment cannot be null",null);
            }
        }
        else {
            return new Result<>(1,"noLogin",null);
        }
    }

    @PostMapping("/slComment/{articleId}")
    Result<Void> addSLComment(@RequestBody SLComment slComment,@PathVariable String articleId,HttpServletRequest request){
        User user = (User) request.getAttribute("user");
        if (isLogin(request)) {
            if (slComment.getComment() != null) {
                try {
                    slComment.setReplyUserId(user.getUserId());
                    slComment.setReplyUserName(user.getUserName());
                    slComment.setReplyAvatar(user.getAvatarUrl());
                    commentService.addSLComment(slComment);
                    commentService.addCommentCounts(articleId);
                    return Result.successWithoutData();
                }catch (Exception e) {
                    e.printStackTrace();
                    return Result.error();
                }
            }else {
                return new Result<>(1,"comment cannot be null",null);
            }
        }
        else {
            return new Result<>(1,"noLogin",null);
        }
    }
    //    对评论进行操作的接口    String articleId,String commentId,String bool
    @PutMapping("likeComment/{articleId}/{which}/{commentId}/{bool}")
    public Result<Void> likeComment(@PathVariable String articleId,@PathVariable String which,@PathVariable String commentId,@PathVariable String bool,HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return new Result<>(1,"noLogin",null);
        }
        else {
            if (which.equals("1")) {    // like:articleId:1 -- flComment:1--true
                try {
                    commentService.likeComment(articleId,"flComment:" + commentId, String.valueOf(user.getUserId()), bool);
                    return Result.successWithoutData();
                }catch (Exception e) {
                    e.printStackTrace();
                    return Result.error();
                }
            }
            else if (which.equals("2")) {
                try {
                    commentService.likeComment(articleId,"slComment:" + commentId, String.valueOf(user.getUserId()),bool);
                    return Result.successWithoutData();
                }catch (Exception e) {
                    e.printStackTrace();
                    return Result.error();
                }
            }
            else {
                return Result.error();
            }
        }
    }
    @PutMapping("dislikeComment/{articleId}/{which}/{commentId}/{bool}")
    public Result<Void> dislikeComment(@PathVariable String articleId,@PathVariable String which,@PathVariable String commentId,@PathVariable String bool,HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return new Result<>(1,"noLogin",null);
        }
        else {
            if (which.equals("1")) {    // like:articleId:1 -- flComment:1--true
                try {
                    commentService.dislikeComment(articleId,"flComment:" + commentId, String.valueOf(user.getUserId()), bool);
                    return Result.successWithoutData();
                }catch (Exception e) {
                    e.printStackTrace();
                    return Result.error();
                }
            }
            else if (which.equals("2")) {
                try {
                    commentService.dislikeComment(articleId,"slComment:" + commentId, String.valueOf(user.getUserId()),bool);
                    return Result.successWithoutData();
                }catch (Exception e) {
                    e.printStackTrace();
                    return Result.error();
                }
            }
            else {
                return Result.error();
            }
        }
    }

    @GetMapping("all-comments/{articleId}")
    Result<List<CommentGroup>> allCommentsOfArticle(@PathVariable Integer articleId,HttpServletRequest request){
        User user = (User) request.getAttribute("user");
        if (user != null) {
            List<CommentGroup> commentGroupList = commentService.getAllCommentsOfArticle(articleId, String.valueOf(user.getUserId()));
            if (commentGroupList != null) {
                return Result.successWithData(commentGroupList);
            }
            else {
                return new Result<>(1,"评论为空",null);
            }
        }
        else {
            List<CommentGroup> commentGroupList = commentService.getAllCommentsOfArticleWithoutLogin(articleId);
            if (commentGroupList != null) {
                return Result.successWithData(commentGroupList);
            }
            else {
                return new Result<>(1,"评论为空",null);
            }
        }
    }

    private boolean isUserAdmin(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        return user != null && user.getRole().equals("2");
    }
    private boolean isLogin(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        return user != null;
    }

    @GetMapping("display-flcomments")
    Result<List<Comments>> displayFLComments(HttpServletRequest request) {
        if (isUserAdmin(request)) {
            List<Comments> comments = commentService.getFLComments();
            return Result.successWithData(comments);
        }
        else {
            return Result.error();
        }
    }

    @GetMapping("display-slcomments")
    Result<List<Comments>> displayAllComments(HttpServletRequest request) {
        if (isUserAdmin(request)) {
            List<Comments> comments = commentService.getSLComments();
            return Result.successWithData(comments);
        }
        else {
            return Result.error();
        }
    }

    @DeleteMapping("delete-comment/{which}/{commentId}")
    Result<Void> deleteFLComment(@PathVariable Integer which,@PathVariable Integer commentId,HttpServletRequest request) {
        if (isUserAdmin(request) && which == 1) {
            try {
                commentService.deleteFLComment(commentId);
                return Result.successWithoutData();
            }catch (Exception e) {
                return Result.error();
            }
        } else if (isUserAdmin(request) && which == 2) {
            try {
                commentService.deleteSLComment(commentId);
                return Result.successWithoutData();
            }catch (Exception e) {
                return Result.error();
            }
        }
        return Result.error();
    }

    @PutMapping("report-comment/{which}/{commentId}/{reason}")
    Result<Void> reportComment(@PathVariable Integer which,@PathVariable Integer commentId,@PathVariable String reason,HttpServletRequest request) {
        if (isLogin(request) && which == 1) {
            try {
                commentService.reportFLComment(reason,commentId);
                return Result.successWithoutData();
            }catch (Exception e) {
                e.printStackTrace();
                return Result.error();
            }

        }
        else if (isLogin(request) && which == 2) {
            try {
                commentService.reportSLComment(reason,commentId);
                return Result.successWithoutData();
            }catch (Exception e) {
                return Result.error();
            }
        }
        return Result.error();
    }

}
