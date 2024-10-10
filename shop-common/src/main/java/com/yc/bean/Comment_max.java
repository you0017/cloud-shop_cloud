package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment_max <T> {
    private List<CommentList> commentLists;//主评论集合
    private Long comment_count=0L;   //评论数量

    //打分人数用的
    private int five=0;
    private int four=0;
    private int three=0;
    private int two=0;
    private int one=0;

    //前端传入的数据
    private int pageno=1;   //当前第几页
    private int pagesize=5; //每页多少条
    private String sortby;  //排序列名
    private String sort;    //asc/desc


    //需要计算的
    private int totalpages; //总页数
    private int pre;    //上一页
    private int next;   //下一页
}
