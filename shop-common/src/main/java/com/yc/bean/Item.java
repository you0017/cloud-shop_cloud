package com.yc.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    @TableId(type = IdType.AUTO)
    private String id;
    private String name;    //商品名
    private Double price;   //价格
    private Integer stock;  //货量
    private String image;   //图片地址
    private String category;    //种类
    private String brand;   //品牌
    private String spec;    //规格
    private int sold;   //销量
    private int comment_count;  //评论数
    private String create_time;
    private String update_time;
    private Integer rating; //评分
    private Integer status; //是否下架 1?0
    private Integer warning_value; //预警值
    private Integer warning_value_status; //预警类型 1?0
    private String stock_up;  // 库存上限
    private String stock_down; //库存下限
    @TableField(exist = false)//这个不在item表
    private List<ItemPic> itempic;//副图
    private String item_details;  //商品详情|描述

}
