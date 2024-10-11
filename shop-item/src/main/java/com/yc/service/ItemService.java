package com.yc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.bean.Item;
import com.yc.bean.OrderDetail;
import com.yc.bean.PageBean;

import java.io.IOException;
import java.util.List;

public interface ItemService extends IService<Item> {
    public List<Item> getCategories();

    public List<Item> getBrands();

    public PageBean<Item> selectByPage(PageBean<Item> pageBean) throws IOException;

    public void fallback(List<OrderDetail> orderDetails);

    public List<Item> association(String association) throws IOException;
}
