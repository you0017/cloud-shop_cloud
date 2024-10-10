package com.yc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.bean.Address;
import com.yc.bean.Comment;
import com.yc.bean.Comment_max;
import jakarta.servlet.http.Part;
import org.springframework.web.multipart.MultipartFile;

public interface CommentService extends IService<Comment> {
    public void likes(String id, String index);

    public Comment_max getComments(String id, String pageNo, String pageSize, String sortBy, String sort);

    public void remark(String id1, String topic, String id, String rating, String remark);

    public String pic(Part file);
}
