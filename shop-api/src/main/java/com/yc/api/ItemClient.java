package com.yc.api;

import com.yc.bean.Item;
import com.yc.bean.OrderDetail;
import com.yc.model.JsonModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("shop-item")
public interface ItemClient {

    @PutMapping("/item/fallback")
    public void fallback(@RequestBody List<OrderDetail> orderDetails);

    @GetMapping("/item/getByIds")
    public List<Item> getByIds(@RequestParam("ids") List<String> ids);
}
