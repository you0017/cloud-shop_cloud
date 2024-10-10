package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageBean<T> {
    //前端传入的数据
    private int pageno=1;   //当前第几页
    private int pagesize=5; //每页多少条
    private String sortby;  //排序列名
    private String sort;    //asc/desc

    private String search;  //搜索关键字


    //查询结果
    private long total; //总记录数
    private List<T> dataset;

    //需要计算的
    private int totalpages; //总页数
    private int pre;    //上一页
    private int next;   //下一页
}
