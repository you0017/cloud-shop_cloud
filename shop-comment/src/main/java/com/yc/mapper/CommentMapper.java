package com.yc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yc.bean.Address;
import com.yc.bean.AdminComment;
import com.yc.bean.Comment;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {

    @Select("select comments.id as id, item_id, user_id, item.name AS name, " +
            "userinformation.accountname AS accountname, " +
            "comments.comment AS comment, " +
            "comments.created_at as created_at, " +
            "comments.rating as rating , " +
            "comments.shop_reply as shop_reply, " +
            "comments.shop_backcomment_status as shop_backcomment_status, " +
            "comments.shop_backcomment as shop_backcomment " +
            " FROM comments JOIN item ON comments.item_id = item.id JOIN userinformation ON comments.user_id = userinformation.id " +
            " where comments.parent_comment_id=0 ORDER BY comments.created_at desc limit #{limit} offset #{skip}")
    public List<AdminComment> allCommentData(int limit, int skip);


}
