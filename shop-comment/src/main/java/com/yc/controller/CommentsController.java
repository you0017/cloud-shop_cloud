package com.yc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.bean.Comment;
import com.yc.bean.CommentList;
import com.yc.bean.Comment_max;
import com.yc.mapper.CommentMapper;
import com.yc.model.JsonModel;
import com.yc.service.CommentService;
import com.yc.utils.AliOSSProperties;
import com.yc.utils.AliOSSUtils;
import com.yc.utils.YcConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@WebServlet("/html/comment.action")
//@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
@RestController
@RequestMapping("/comment")
public class CommentsController {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private CommentService commentService;

    /**
     * 点赞或踩
     */
    @PostMapping("/likes")
    public JsonModel likes(@RequestParam("id")String id,@RequestParam("index")String index) {
        commentService.likes(id,index);
        return JsonModel.ok();
    }

    /**
     * 获取评论
     */
    @GetMapping("/getComments")
    public JsonModel getComments(@RequestParam("id")String id,
                                 @RequestParam("pageno")String pageNo,
                                 @RequestParam("pagesize")String pageSize,
                                 @RequestParam("sortby")String sortBy,
                                 @RequestParam("sort")String sort) {
        Comment_max commentMax = commentService.getComments(id,pageNo,pageSize,sortBy,sort);
        return JsonModel.ok().setDate(commentMax);
    }

    /**
     * 评论
     */
    @PostMapping("/remark")
    public JsonModel remark(@RequestParam("id")String id1,//主评论id
                            @RequestParam("topic")String topic,
                            @RequestParam("item_id")String id,
                            @RequestParam("rating")String rating,
                            @RequestParam("remark")String remark) {

        commentService.remark(id1,topic,id,rating,remark);
        return JsonModel.ok();
    }

    /**
     * 图片上传
     */
    @PostMapping("/pic")
    public Map pic(HttpServletRequest req,HttpServletResponse resp) {

        String url = null;
        try {
            List<Part> parts = (ArrayList) req.getParts();
            url = commentService.pic(parts.get(0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        Map map = new HashMap();
        map.put("uploaded","1");
        map.put("url",url);
        return map;
    }

    @GetMapping("/comment/getById")
    public List<Comment> getById(@RequestParam("id") String id){
        LambdaQueryWrapper<Comment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Comment::getUser_id, id);
        return commentMapper.selectList(lambdaQueryWrapper);
    }
}
