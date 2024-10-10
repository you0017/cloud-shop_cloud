package com.yc.api;

import com.yc.bean.OrderDetail;
import com.yc.bean.UserInformation;
import com.yc.model.JsonModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("shop-order")
public interface OrderClient {
    @GetMapping("/order/hasUserPurchasedProduct")
    public boolean hasUserPurchasedProduct(@RequestParam("id") String id,@RequestParam("userId") String userId);


    @GetMapping("/order/admin/getUserInformationByOrderId")
    public UserInformation getUserInformationByOrderId(@RequestParam("orderId") Integer orderId);

    @GetMapping("/order/admin/getOrderDetailsByOrderId")
    public List<OrderDetail> getOrderDetailsByOrderId(@RequestParam("orderId") Integer orderId);


}
