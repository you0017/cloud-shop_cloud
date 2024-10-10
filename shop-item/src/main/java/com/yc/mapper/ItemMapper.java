package com.yc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yc.bean.Item;
import com.yc.bean.Sale;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ItemMapper extends BaseMapper<Item> {

    @Select("SELECT `name`, price, price * sold AS total_price, sold  FROM item limit #{limit} offset #{skip}")
    public List<Sale> getAllrSaleData(int limit, int skip);
}
