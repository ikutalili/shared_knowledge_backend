package com.yuki.controller;

import com.yuki.entity.Article;
import com.yuki.entity.Result;
import com.yuki.entity.User;
import com.yuki.service.ArticleService;
import com.yuki.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private CommentService commentService;

    private boolean isLogin(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        return user != null;
    }
    @GetMapping("list/{type}")
    public Result<List<Article>> getArticleInfo(@PathVariable String type, HttpServletRequest request) {
        User user = (User) request.getAttribute("user") ;
        List<Article> articles;
        //        通过文章类别去查询每一类别下的所有文章
//       List<Article> articles = articleService.getArticlesInfoWithType(type,userId);
       if (user != null) {
           articles = articleService.getArticlesInfoWithType(type, String.valueOf(user.getUserId()));
           System.out.println(user.getUserId());
           if (articles != null) {
               return Result.successWithData(articles);
           }
           else {
               return Result.error();
           }
       }
       else {
//           如果用户未登录，返回默认数据
           articles = articleService.getArticlesInfoByTypeWithoutLogin(type);
           if (articles != null) {
               return Result.successWithData(articles);
           }
           else {
               return Result.error();
           }
       }

    }
//    对文章进行操作的接口    对点赞等一系列高频操作的存取都只对redis操作，此处是 存 ，true or false 都在这里
    @PutMapping("operation-to-article/{articleId}/{operation}/{bool}")
    public Result<Void> operationToArticle(@PathVariable String articleId,@PathVariable String operation,@PathVariable Boolean bool,HttpServletRequest request) {
        User user = (User) request.getAttribute("user") ;
        if (user != null) {
            try {
                articleService.operationToArticle(String.valueOf(user.getUserId()),articleId,operation,bool,user.getUserName());
                return Result.successWithoutData();
            }
            catch (Exception e) {
                return Result.error();
            }
        }else {
            return new Result<>(1,"noLogin",null);
        }
    }
    @GetMapping("display-all-articles")
    public Result<List<Article>> displayAllArticles(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user != null && user.getRole().equals("2")) {
            List<Article> allArticles = articleService.getAllArticles();
            return Result.successWithData(allArticles);
        }
        else {
            return Result.error();
        }

    };

    @PutMapping("report-article/{reason}/{articleId}")
    public Result<Void> reportArticle(@PathVariable String reason,@PathVariable Integer articleId,HttpServletRequest request) {
        if(isLogin(request)) {
            try {
                articleService.reportArticle(reason,articleId);
                return Result.successWithoutData();
            }catch (Exception e) {
                e.printStackTrace();
                return Result.error();
            }
        }
        else {
            return new Result<>(1,"noLogin",null);
        }
    }

    @DeleteMapping("delete-article/{id}")
    public Result<Void> deleteArticle(@PathVariable Integer id,HttpServletRequest request) {
        if (isLogin(request)) {
            try {
                articleService.deleteArticle(id);
                commentService.deleteCommentOfArticle(id);
                return Result.successWithoutData();
            }catch (Exception e) {
                e.printStackTrace();
                return Result.error();
            }
        }
        return Result.error();
    }

    @PutMapping("click-article/{articleId}")
    public void ratingArticle(HttpServletRequest request,@PathVariable String articleId) {
        User user = (User) request.getAttribute("user");
        if (isLogin(request)) {
            articleService.ratingArticle(articleId, String.valueOf(user.getUserId()));
            System.out.println("点击文章评分操作");
            System.out.println("success-------");
        }
        else {
            System.out.println("点击文章评分操作失败");
            System.out.println("failed--------");
        }
    }
}
