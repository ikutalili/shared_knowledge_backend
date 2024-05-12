package com.yuki.controller;

import com.yuki.entity.Article;
import com.yuki.entity.Result;
import com.yuki.entity.User;
import com.yuki.service.ArticleService;
import com.yuki.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@RestController
public class FileController {

    private static final String avatarPath = "C:\\Users\\凛\\Desktop\\Shared_knowledge\\avatars\\";
    private static final String articleSavePath = "C:\\Users\\凛\\Desktop\\Shared_knowledge\\articles\\";
    private static final String articleCoverPath = "C:\\Users\\凛\\Desktop\\Shared_knowledge\\covers\\";
    private static final String articleImagesPath = "C:\\Users\\凛\\Desktop\\Shared_knowledge\\article-images\\";
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService;
// 上传文件的方法,此处上传用户头像
    @PostMapping("/upload/{userId}")
    public Result<String> upload(MultipartFile file, @PathVariable Integer userId) {
        if (!file.isEmpty()){ // whether the file is null
            try {
//                获取该id在数据库中存储的头像名字，看看是否是默认的，如果该文件存在，且不是默认的名字，则上传修改，并删除原来的
                String avatarFileName = userService.getAvatarFileName(userId);
                System.out.println(avatarFileName + "============ avatar-fileName");
                File f = new File(avatarPath + avatarFileName);
//                如果该文件存在，且不是默认的名字，则上传修改，并删除原来的
                if (!Objects.equals(avatarFileName, "gintoki.jpg") && f.exists()) {
                    boolean delete = f.delete();
                    System.out.println("删除文件---" + delete);
                }
                // 获取上传的文件的原名称
                String originalFileName = file.getOriginalFilename();
                // 使用uuid加后缀作为文件名保存
                String fileName = UUID.randomUUID().toString() + originalFileName.substring(originalFileName.lastIndexOf(".")) ;
               // 定义文件存放路径
                File dest = new File("C:\\Users\\凛\\Desktop\\Shared_knowledge\\avatars\\"+fileName);
                //
                file.transferTo(dest);

                userService.updateUserAvatar(userId,fileName);

//                为什么返回文件名呢？这是因为访问图片采用的是接口+文件参数名的形式，此文件名（头像名）返回给前端可以
//                持久性地存储在全局用户信息里面。
                return Result.successWithData(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("-----------------运行出错");
                return Result.error();
            }
        }
        else{
            System.out.println("file is null...");
            return Result.error();
        }
    }
//    上传文章封面或者图片
    @PostMapping("upload-article-image/{pathId}")
    public String uploadArticleImages(MultipartFile file,@PathVariable Integer pathId) {
        if (!file.isEmpty()){ // whether the file is null
            try {
                if (pathId == 1) {
                    // 获取上传的文件的原名称
                    String originalFileName = file.getOriginalFilename();
                    // 使用uuid加后缀作为文件名保存
                    String fileName = UUID.randomUUID() + originalFileName.substring(originalFileName.lastIndexOf(".")) ;
                    // 定义文件存放路径
                    File dest = new File(articleCoverPath + fileName);
                    //
                    file.transferTo(dest);

                    articleService.addArticleCover(fileName);
//                为什么返回文件名呢？这是因为访问图片采用的是接口+文件参数名的形式，此文件名（头像名）返回给前端可以
//                持久性地存储在全局用户信息里面。
                    System.out.println( "http://localhost:8080/avatar-image/" + fileName);
                    return "http://localhost:8080/avatar-image/" + fileName;
                }
                else if (pathId == 2) {
                    // 获取上传的文件的原名称
                    String originalFileName = file.getOriginalFilename();
                    // 使用uuid加后缀作为文件名保存
                    String fileName = UUID.randomUUID() + originalFileName.substring(originalFileName.lastIndexOf(".")) ;
                    // 定义文件存放路径
                    File dest = new File(articleImagesPath + fileName);
                    //
                    file.transferTo(dest);

//                为什么返回文件名呢？这是因为访问图片采用的是接口+文件参数名的形式，此文件名（头像名）返回给前端可以
//                持久性地存储在全局用户信息里面。
                    return "http://localhost:8080/avatar-image/" + fileName;
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("-----------------运行出错");
                return null;
            }
        }
        else{
            System.out.println("file is null...");
            return null;
        }
        return null;
    }
// 把文章保存到本地磁盘，但需要将路径与用户一一对应起来
    @PostMapping("/save-article")
    Result saveArticle(Integer id,String name,String avatar,String title,String type,String article,String preview){

        String fileName = UUID.randomUUID().toString();
        String articlePath = "C:\\Users\\凛\\Desktop\\Shared_knowledge\\articles\\"+fileName + ".html";
//        紧接着把文件路径存放到数据库中
        try {
            // 创建 FileWriter 对象，并指定要写入的文件路径
            FileWriter writer = new FileWriter(articlePath);
            // 将文本数据写入文件
            writer.write(article);
//            关闭文件流操作
            writer.close();

//            数据库中的filename是没有后缀名的，因此后面的获取文章要自己加上去
            articleService.saveArticle(id,name,avatar,title,fileName,type,preview);
            return Result.successWithoutData();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error();
        }

//        return Result.successWithoutData();
    }

    // 展示文章
    @GetMapping("/article/{title}")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> getArticleContent(@PathVariable String title) {
        File htmlFile = new File(articleSavePath + title + ".html");
        if (!htmlFile.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        StreamingResponseBody stream = out -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(htmlFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.write(line.getBytes()); // 将字符串转换为字节数组并写入输出流
                }
            } catch (IOException e) {
                // 处理异常
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(stream);
    }
// 文件下载--加了响应头
@GetMapping("/user-image/{title}")
public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String title) {

//    ResponseEntity 是一个http响应对象
//        根据唯一id从数据库中查出该文件的路径
    String filePath = articleSavePath + title;

    try {
        Path path = Paths.get(filePath);
        // 读取文件内容为字节数组
        byte[] data = Files.readAllBytes(path);
        // 创建 ByteArrayResource 对象
        ByteArrayResource resource = new ByteArrayResource(data);

        // 获取文件名
        String fileName = path.getFileName().toString();

        // 构建响应头
        HttpHeaders headers = new HttpHeaders();
// 需要返回什么类型的文件，在这里指定就行
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        
        // 构建 ResponseEntity 并返回
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    } catch (IOException e) {
        e.printStackTrace();
        // 如果文件读取失败，返回 HTTP 状态码 500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
    }
}
/*
* 是的，这个代码是通用的，可以用于下载任何类型的文件，包括文档、PDF、图片、视频等。主要是通过设置
* 响应头的 `Content-Type` 来告诉浏览器返回的是什么类型的文件，以及通过 `Content-Disposition` 头部来告诉浏览器将文件作为附件进行下载。
在代码中，我们设置了 `Content-Type` 为 `application/octet-stream`，表示二进制流文件，这个类型适用于大多数文件类型。
* 对于不同类型的文件，你可以根据文件的 MIME 类型设置不同的 `Content-Type`，比如：

- 对于文档（如.doc、.docx）、PDF 文件，可以使用 `application/msword` 或 `application/pdf`。
- 对于图片文件（如.jpg、.png、.gif），可以使用 `image/jpeg` 或 `image/png`。
- 对于视频文件（如.mp4、.avi、.mov），可以使用 `video/mp4` 或 `video/avi`。

如果你能确定文件的 MIME 类型，最好使用适当的 MIME 类型来设置 `Content-Type`，这样可以让浏览器更好地识别文件类型。
* 但即使你使用通用的 `application/octet-stream`，大多数浏览器仍然会根据文件的后缀名来判断文件类型并进行下载。

总之，以上的代码可以用于下载各种类型的文件，你只需要根据文件类型设置适当的 `Content-Type` 即可。
* */

//    图片的src地址获取接口,这个不属于axios请求，不会加上token
    @GetMapping("/avatar-image/{id}/{imageName}")
    public ResponseEntity<Resource> avatarImageUrl(@PathVariable Integer id, @PathVariable String imageName, HttpServletRequest request) {
//        这里的名字包含后缀名
            String imgPathInDB = avatarPath + imageName;
//        这里的id，以后凡是头像的src请求接口，都加上 1 ，文章封面的src 请求接口，则加上 2
            if (id == 2) {
                imgPathInDB = articleCoverPath + imageName;
            }

//  创建一个path对象
            Path imgPath = Paths.get(imgPathInDB);
            try {
                Resource resource = new InputStreamResource(Files.newInputStream(imgPath));

//  构建http响应头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);

//  返回响应对象
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("-----------返回失败");
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                return ResponseEntity.status(404).body(null);
            }
    }
}
