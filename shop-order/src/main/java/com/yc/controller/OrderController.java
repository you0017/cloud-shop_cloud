package com.yc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yc.bean.*;
import com.yc.config.AlipayConfig;
import com.yc.context.BaseContext;
import com.yc.mapper.OrderMapper;
import com.yc.mapper.ReturnOrdersMapper;
import com.yc.model.JsonModel;
import com.yc.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

//@WebServlet("/html/order.action")
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ReturnOrdersMapper returnOrdersMapper;
    @Autowired
    private OrderService orderService;

    /**
     * 获取退货单详情
     */
    @GetMapping("/getReturnOrder")
    public JsonModel getReturnOrder() {
        String userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ReturnOrder> lambda = new LambdaQueryWrapper<>();
        lambda.eq(ReturnOrder::getCustomer_id,userId)
                .orderByDesc(ReturnOrder::getReturn_id);
        List<ReturnOrder> list = returnOrdersMapper.selectList(lambda);

        return JsonModel.ok().setDate(list);
    }


    /**
     *
     * @param op//操作
     * @param item_id//商品id
     * @param order_id//订单id
     * @param reason//退货原因
     * @param trackingCompany//公司
     * @param trackingNumber//快递单号
     * @param return_id//退货单id
     * @return
     */
    @PostMapping("/ret")
    public JsonModel ret(@RequestParam("index")String op,@RequestParam(value = "item_id",defaultValue = "")String item_id,
                         @RequestParam(value = "order_id",defaultValue = "")String order_id,@RequestParam(value = "reason",defaultValue = "")String reason,
                         @RequestParam(value = "tracking_company",defaultValue = "")String trackingCompany,@RequestParam(value = "tracking_number",defaultValue = "")String trackingNumber,
                         @RequestParam(value = "return_id",defaultValue = "")String return_id) {

        boolean result = orderService.ret(op, item_id, order_id, reason, trackingCompany, trackingNumber, return_id);

        if (result){
            return JsonModel.ok();
        }else {
            return JsonModel.error("操作失败");
        }
    }
    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public JsonModel cancel(@RequestParam("reason") String reason,@RequestParam("item_id")String id) {
        //取消原因
        //订单号
        orderService.cancel(reason,id);
        return JsonModel.ok();
    }
    /**
     * 确认收货
     */
    @PutMapping("/get")
    public JsonModel get(@RequestParam("id") String id) throws IOException {
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Order::getStatus,4)
                .set(Order::getUpdate_time,LocalDateTime.now())
                .set(Order::getEnd_time,LocalDateTime.now())
                .eq(Order::getId,id);
        orderMapper.update(null,wrapper);
        return JsonModel.ok();
    }

    /**
     * 获取所有订单
     */
    @GetMapping("/getOrder")
    public JsonModel getOrder(@RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize")Integer pageSize) {
        List<OrderList> list = orderService.getOrder(pageNo,pageSize);
        return JsonModel.ok().setDate(list);
    }
    /**
     * 支付
     * 现在这段支付由AliPayController处理
     */
    @PostMapping("/pay")
    public JsonModel pay(@RequestBody OrderVO orderVO,@RequestParam("protocol")String protocol,@RequestParam("host")String host) {
        Order order = orderService.pay(orderVO);

        /**
         * 修改沙箱支付return地址
         */

        AlipayConfig.notify_url = protocol + "://" + host + "/html/index.html?status=1";
        AlipayConfig.return_url = protocol + "://" + host + "/html/index.html?status=1";

        return JsonModel.ok().setDate(order);
    }


    /**
     * 判断用户是否购买了此商品，评论用
     */
    @GetMapping("/hasUserPurchasedProduct")
    public boolean hasUserPurchasedProduct(String id, String userId){
        return orderService.hasUserPurchasedProduct(id,userId);
    }
}
