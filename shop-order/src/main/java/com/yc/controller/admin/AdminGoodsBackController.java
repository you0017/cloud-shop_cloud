package com.yc.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yc.api.ItemClient;
import com.yc.bean.*;
import com.yc.mapper.OrderDetailMapper;
import com.yc.mapper.ReturnOrdersMapper;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@WebServlet("/admin/goodsback.action")
@RestController
@RequestMapping("/goodsback/admin")
public class AdminGoodsBackController {
    @Autowired
    private ReturnOrdersMapper returnOrdersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ItemClient itemClient;

    // 退款的操作
    @RequestMapping("/over")
    @GlobalTransactional
    public DataModel over(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String backType = request.getParameter("back_type");  // 退货类型 1为退款，2为退货退款
        String returnId = request.getParameter("return_id");   // 退货单号
        String returnQuantity = request.getParameter("return_quantity");  // 退货数量
        String realityBackMoney = request.getParameter("realityBackMoney");  // 实际退款金额
        String productId = request.getParameter("product_id");  // 商品id

        if ("仅退款".equals(backType) ) {  // 仅退款
            LambdaUpdateWrapper<ReturnOrder> db = new LambdaUpdateWrapper<>();
            db.set(ReturnOrder::getReturn_status, 4)
                    .eq(ReturnOrder::getReturn_id, returnId);
            int result = returnOrdersMapper.update(null, db); // 修改退货单状态为已退款
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("仅退款操作成功");
            } else {
                ud.setCode(1);
                ud.setMsg("仅退款操作失败");
            }
        } else if ("退货退款".equals(backType)){  // 退货退款
            OrderDetail orderDetails = orderDetailMapper.selectById(returnId);// 修改库存
            List<OrderDetail> orderDetailsList = new ArrayList<>();
            orderDetailsList.add(orderDetails);
            try {
                itemClient.fallback(orderDetailsList);
            } catch (Exception e) {
                throw new RuntimeException("回退商品数量失败");
            }

            if (1 == 1) {
                LambdaUpdateWrapper<ReturnOrder> db = new LambdaUpdateWrapper<>();
                db.set(ReturnOrder::getReturn_status, 3)
                        .eq(ReturnOrder::getReturn_id, returnId);
                int result = returnOrdersMapper.update(null, db); // 修改退货单状态为已退款
                if (result >= 1) {
                    ud.setCode(0);
                    ud.setMsg("退货退款操作成功");
                } else {
                    ud.setCode(1);
                    ud.setMsg("退货退款操作失败");
                }
            } else {
                ud.setCode(1);
                ud.setMsg("退货退款操作失败");
            }
        }else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        return ud;
    }


    // 不通过退货请求
    @RequestMapping("/no")
    public DataModel no(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String return_id = request.getParameter("return_id");  // 退货单号

        LambdaUpdateWrapper<ReturnOrder> db = new LambdaUpdateWrapper<>();
        db.set(ReturnOrder::getReturn_status, 5)
                .eq(ReturnOrder::getReturn_id, return_id);
        int result = returnOrdersMapper.update(null, db);

        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("操作成功");
        } else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        return ud;
    }

    // 通过退货申请
    @RequestMapping("/ok")
    public DataModel ok(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String return_id = request.getParameter("return_id");  // 退货单号
        String backType = request.getParameter("back_type");  // 退货类型
        if (2 == Integer.parseInt(backType)) {  // 退货退款
            LambdaUpdateWrapper<ReturnOrder> db = new LambdaUpdateWrapper<>();
            db.set(ReturnOrder::getReturn_status, 2)
                    .eq(ReturnOrder::getReturn_id, return_id);
            int result = returnOrdersMapper.update(null, db);
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("操作成功");
            } else {
                ud.setCode(1);
                ud.setMsg("操作失败");
            }
        }
        if (1 == Integer.parseInt(backType)) {  // 退货
            // 这里还要加上退款的逻辑代码
            LambdaUpdateWrapper<ReturnOrder> db = new LambdaUpdateWrapper<>();
            db.set(ReturnOrder::getReturn_status, 3)
                    .eq(ReturnOrder::getReturn_id, return_id);
            int result = returnOrdersMapper.update(null, db);
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("操作成功");
            } else {
                ud.setCode(1);
                ud.setMsg("操作失败");
            }
        }
        return ud;
    }


    //
    @RequestMapping("/backGoods")
    public DataModel backGoods(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String aaa = request.getParameter("AAA");  // 1为同意，2为不同意
//        String returnQuantity = request.getParameter("return_quantity");  // 退货数量
        String return_id = request.getParameter("return_id");  // 退货单号
//        String orderId = request.getParameter("order_id");  // 订单号
//        String productId = request.getParameter("product_id");   // 商品id
        if (aaa == "1") {   // 如果同意，把售后订单改为处理中    把订单详情表改为处理中

            LambdaUpdateWrapper<ReturnOrder> db = new LambdaUpdateWrapper<>();
            db.set(ReturnOrder::getReturn_status, 2)
                    .eq(ReturnOrder::getReturn_id, return_id);
            int result = returnOrdersMapper.update(null, db);

            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("操作成功");
                return ud;
            }
            ud.setCode(1);
            ud.setMsg("操作失败");
            return ud;
        }

        if (aaa == "2") {   // 如果不同意，把售后订单改为不予退货
            LambdaUpdateWrapper<ReturnOrder> db = new LambdaUpdateWrapper<>();
            db.set(ReturnOrder::getReturn_status, 5)
                    .eq(ReturnOrder::getReturn_id, return_id);
            int result = returnOrdersMapper.update(null, db);

            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("操作成功");
                return ud;
            }
            ud.setCode(1);
            ud.setMsg("操作成功");
            return ud;
        }
        ud.setCode(1);
        ud.setMsg("操作失败");
        return ud;
    }


    // 查看售后单号的数据    只查看审核通过的的售后单号
    @RequestMapping("/getOnlyMoney")
    public DataModel getOnlyMoney(HttpServletRequest request, HttpServletResponse resposne) {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);

        List<AdminReturnOrder> select = returnOrdersMapper.getOnlyMoney(limit, skip);

        LambdaQueryWrapper<ReturnOrder> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.in(ReturnOrder::getReturn_status, 3, 4);
        Long total = returnOrdersMapper.selectCount(lambdaQueryWrapper);

        if (select != null && select.size() > 0) {
            ud.setCode(0);
            ud.setMsg("查询成功");
            ud.setData(select);
            ud.setCount(Math.toIntExact(total));
        } else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        return ud;
    }

    // 查所有申请退款的售后单号的数据  只查看待处理、不同意退货的单子
    @RequestMapping("/getGoodsAndMoneyData")
    public DataModel getGoodsAndMoneyData(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);

        List<AdminReturnOrder> select = returnOrdersMapper.getGoodsAndMoneyData(limit, skip);

        LambdaQueryWrapper<ReturnOrder> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.in(ReturnOrder::getReturn_status, 1, 5);
        Long total = returnOrdersMapper.selectCount(lambdaQueryWrapper);


        if (select != null && select.size() > 0) {
            ud.setCode(0);
            ud.setMsg("查询成功");
            ud.setData(select);
            ud.setCount(Math.toIntExact(total));
        } else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        return ud;
    }
}
