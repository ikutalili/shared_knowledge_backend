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
    Result<Void> addFLComment(@RequestBody FLComment flComment,@PathVariable String articleId){
        try {
            commentService.addFLComment(flComment);
            commentService.addCommentCounts(articleId);
        }catch (Exception e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.successWithoutData();
    }

    @PostMapping("/slComment/{articleId}")
    Result<Void> addSLComment(@RequestBody SLComment slComment,@PathVariable String articleId){
        try {
            commentService.addSLComment(slComment);
            commentService.addCommentCounts(articleId);
        }catch (Exception e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.successWithoutData();
    }
    //    对评论进行操作的接口    String articleId,String commentId,String bool
    @PutMapping("likeComment/{articleId}/{userId}/{which}/{commentId}/{bool}")
    public Result<Void> likeComment(@PathVariable String articleId,@PathVariable String which,@PathVariable String commentId,@PathVariable String userId,@PathVariable String bool) {
        if (which.equals("1")) {    // like:articleId:1 -- flComment:1--true
            try {
                commentService.likeComment(articleId,"flComment:" + commentId,userId, bool);
            }catch (Exception e) {
                e.printStackTrace();
                return Result.error();
            }
        }
        else if (which.equals("2")) {
            try {

                commentService.likeComment(articleId,"slComment:" + commentId, userId,bool);
            }catch (Exception e) {
                e.printStackTrace();
                return Result.error();
            }
        }
        return Result.successWithoutData();
    }
    @PutMapping("dislikeComment/{articleId}/{userId}/{which}/{commentId}/{bool}")
    public Result<Void> dislikeComment(@PathVariable String articleId,@PathVariable String which,@PathVariable String commentId,@PathVariable String userId,@PathVariable String bool) {
        if (which.equals("1")) {    // like:articleId:1 -- flComment:1--true
            try {
                commentService.dislikeComment(articleId,"flComment:" + commentId,userId, bool);
            }catch (Exception e) {
                e.printStackTrace();
                return Result.error();
            }
        }
        else if (which.equals("2")) {
            try {
                System.out.println(!redisUtils.getDisLikeStatusOfComment(userId,commentId) && Boolean.parseBoolean(bool));
                System.out.println(!redisUtils.getDisLikeStatusOfComment(userId,commentId));
                System.out.println(bool);
                commentService.dislikeComment(articleId,"slComment:" + commentId, userId,bool);
            }catch (Exception e) {
                e.printStackTrace();
                return Result.error();
            }
        }
        return Result.successWithoutData();
    }

    @GetMapping("all-comments/{articleId}/{userId}")
    Result<List<CommentGroup>> allCommentsOfArticle(@PathVariable Integer articleId,@PathVariable String userId){
        List<CommentGroup> commentGroupList = commentService.getAllCommentsOfArticle(articleId,userId);
        return Result.successWithData(commentGroupList);
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
