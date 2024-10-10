package com.yc.bean;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("comments")
public class AdminComment {
    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private String accountname;
    private String comment;
    private String created_at;
    private Integer rating;
    private Integer shop_reply;  // 审核     0待审核  1 不通过  2 通过
    private Integer item_id;
    private Integer user_id;
    private Integer id;
    private Integer shop_backcomment_status; // 商家回复状态  0 未回复  1 已回复
    private String shop_backcomment; // 商家回复内容
    private String shop_backcomment_time; // 商家回复时间
}
