package com.yc.api;

import com.yc.bean.Comment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("shop-comment")
public interface CommentClient {

    @GetMapping("/comment/getById")
    public List<Comment> getById(@RequestParam("id") String id);
}
