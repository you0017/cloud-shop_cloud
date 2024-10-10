package com.yc.controller.admin;


// 订单的操作

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yc.bean.*;
import com.yc.mapper.LogisticsMapper;
import com.yc.mapper.OrderDetailMapper;
import com.yc.mapper.OrderMapper;
import com.yc.mapper.ReturnOrdersMapper;
import com.yc.model.JsonModel;
import com.yc.utils.JmsMessageProducer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.executor.BatchResult;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

//@WebServlet("/admin/order.action")
@RestController
@RequestMapping("/order/admin")
@Transactional
public class AdminOrderController {

    @Autowired
    private ReturnOrdersMapper returnOrdersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private LogisticsMapper logisticsMapper;
    @Autowired
    private JmsMessageProducer jmsMessageProducer;

    // 允许退货
    public void okView(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 查询订单表里的数据
        DataModel ud = new DataModel();
        String id = request.getParameter("id");
    }


    // 申请退货订单的数据
    @RequestMapping("/getAllBackOrderData")
    public DataModel getAllBackOrderData(HttpServletRequest request, HttpServletResponse response) {
        // 查询订单表里的数据
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);

        Long total = returnOrdersMapper.selectCount(null);

        LambdaUpdateWrapper<ReturnOrder> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.last("LIMIT " + limit + " OFFSET " + skip);
        List<ReturnOrder> list = returnOrdersMapper.selectList(lambdaUpdateWrapper);

        ud.setCount(Math.toIntExact(total));
        ud.setData(list);
        ud.setCode(0);

        return ud;
    }


    // 恢复已删除订单
    @RequestMapping("/allRecover")
    public DataModel allRecover(HttpServletRequest request, HttpServletResponse response) {
        // 订单表里改状态
        // 商品表里也要改数量  多表查询
        // 查出订单表里的详情数据  商品ID 购买数量
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");

        LambdaUpdateWrapper<Order> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.in(Order::getId, idsStr).set(Order::getStatus, 4);
        int i = orderMapper.update(null, lambdaUpdateWrapper);


        if (i >= 1) {
            ud.setCode(0);
        } else {
            ud.setCode(1);
            ud.setMsg("恢复失败");
        }
        return ud;
    }

    // 批量发货
    @RequestMapping("/multiSend")
    public DataModel multiSend(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        String sendCompany = request.getParameter("send_company");
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedTime = currentTime.format(formatter);  // 发货时间
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");


        // 生成快递单
        Random random = new Random();
        String seng_id = String.valueOf(random.nextInt(300000));   // 商家生成的快递单号

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 2)
                .in(Order::getId, strArray);
        List<Order> select = orderMapper.selectList(wrapper);
        // 更新状态
        LambdaUpdateWrapper<Order> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(Order::getStatus, 3)
                .set(Order::getSend_company, sendCompany)
                .set(Order::getConsign_time, formattedTime)
                .in(Order::getId, strArray);


        List<Logistics> list = new ArrayList<>();
        for (Order order : select) {
            Logistics logistics = Logistics.builder()
                    .shipping_status(1)
                    .receiver_name(order.getContact())
                    .receiver_phone(order.getMobile())
                    .receiver_address(order.getAddress())
                    .logistics_company(order.getSend_company())
                    .tracking_number(seng_id)
                    .remarks("无备注")
                    .build();
            list.add(logistics);

        }
        List<BatchResult> insert = logisticsMapper.insert(list);

        if (insert.size() < 1) {
            ud.setCode(1);
            ud.setMsg("物流订单生成失败");
            return ud;
        }

        int i = orderMapper.update(null, lambdaUpdateWrapper);


        // 更新时间和公司
        if (i >= 1) {
            ud.setCode(0);
            jmsMessageProducer.shipments(Arrays.stream(strArray)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList()));
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }

    // 全部发货
    @RequestMapping("/allSend")
    public DataModel allSend(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();

        // 所有代发货订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 2);
        List<Order> list = orderMapper.selectList(wrapper);


        Random random = new Random();
        List<Logistics> logistics = new ArrayList<>();
        List<Integer> integerList = new ArrayList<>();
        for (Order order : list) {
            String seng_id = String.valueOf(random.nextInt(300000));   // 商家生成的快递单号
            integerList.add(order.getId());
            Logistics logistics1 = Logistics.builder()
                    .shipping_status(1)
                    .receiver_name(order.getContact())
                    .receiver_phone(order.getMobile())
                    .receiver_address(order.getAddress())
                    .logistics_company(order.getSend_company())
                    .tracking_number(seng_id)
                    .remarks("无备注")
                    .build();
            logistics.add(logistics1);
        }
        List<BatchResult> insert = logisticsMapper.insert(logistics);
        if (insert.size() < 1) {
            ud.setCode(1);
            ud.setMsg("物流订单生成失败");
            return ud;
        }


        String sendCompany = request.getParameter("send_company");
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedTime = currentTime.format(formatter);  // 发货时间

        LambdaUpdateWrapper<Order> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(Order::getStatus, 3)
                .set(Order::getSend_company, sendCompany)
                .set(Order::getConsign_time, formattedTime)
                .eq(Order::getStatus, 2);
        int i = orderMapper.update(null, lambdaUpdateWrapper);

        if (i >= 1) {
            ud.setCode(0);
            jmsMessageProducer.shipments(integerList);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }

    // 发货功能
    @RequestMapping("/recover")
    public DataModel recover(HttpServletRequest request, HttpServletResponse response) {
        // 订单表里改状态
        // 发货表里面
        DataModel ud = new DataModel();
        String userId = request.getParameter("user_id");  // 用户id
        String order_id = request.getParameter("order_id");   // 订单号
        String userName = request.getParameter("user_name");  // 用户名
        String orderAddress = request.getParameter("order_address");  // 收货地址
        String mobile = request.getParameter("mobile");   // 收货电话
        String sendCompany = request.getParameter("send_company");   // 商家选的快递公司
        String remark = request.getParameter("remark");  // 商家备注
        Random random = new Random();
        String seng_id = String.valueOf(random.nextInt(300000));   // 商家生成的快递单号


        // 生成物流订单
        Logistics logistics = new Logistics();
        logistics.setShipping_status(1); // 设置物流状态
        logistics.setReceiver_name(userName); // 设置收货人
        logistics.setReceiver_phone(mobile); // 设置电话
        logistics.setReceiver_address(orderAddress); // 设置地址
        logistics.setLogistics_company(sendCompany); // 设置物流公司
        logistics.setTracking_number(seng_id); // 设置快递单号
        logistics.setRemarks(remark); // 设置备注

        // 执行插入
        int logisticsResult = logisticsMapper.insert(logistics);



        // 如果物流订单生成失败
        if (logisticsResult < 1) {
            ud.setCode(1);
            ud.setMsg("物流订单生成失败");
            return ud;
        }

        // 修改交易订单状态
        Order order = Order.builder()
                .status(3)
                .send_id(seng_id)
                .send_company(sendCompany)
                .id(Integer.parseInt(order_id))
                .build();
        int i = orderMapper.update(order, null);

        if (i >= 1) {
            ud.setCode(0);
            ud.setMsg("发货成功");
            jmsMessageProducer.shipments(Arrays.asList(Integer.valueOf(order_id)));
        } else {
            ud.setCode(1);
            ud.setMsg("发货失败");
        }
        return ud;
    }

    // 模糊查询
    @RequestMapping("/fuzzyQuery")
    public DataModel fuzzyQuery(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String id = request.getParameter("id");// 账号
        String status = request.getParameter("status");// 状态

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

        // 处理 user_id
        if (id != null && !id.trim().isEmpty()) {
            queryWrapper.eq(Order::getUser_id, id.trim());
        }

        // 处理 status
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq(Order::getStatus, status.trim());
        } else {
            queryWrapper.le(Order::getStatus, 4); // status <= 4
        }

        // 处理时间范围
        if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
            queryWrapper.between(Order::getCreate_time, startTime.trim(), endTime.trim());
        } else if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.gt(Order::getCreate_time, startTime.trim());
        } else if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.lt(Order::getCreate_time, endTime.trim());
        }

        // 执行查询
        List<Order> select = orderMapper.selectList(queryWrapper);

        int total = select.size();
        if (select != null && select.size() > 0) {
            ud.setData(select);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }

    // 单个删除
    @RequestMapping("/del")
    public DataModel del(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();

        String idsStr = request.getParameter("idsStr");

        Order order = Order.builder()
                .status(5)
                .id(Integer.parseInt(idsStr))
                .build();
        int i = orderMapper.update(order, null);

        if (i >= 1) {
            ud = allOrderData(ud, request);
            ud.setCode(0);
            ud.setMsg("删除成功");
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }

    //  批量删除 不做删除    状态码变为7
    @RequestMapping("/batchDel")
    public DataModel batchDel(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");

        LambdaUpdateWrapper<Order> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper
                .set(Order::getStatus, 5)
                .in(Order::getId, strArray);
        int i = orderMapper.update(null, lambdaUpdateWrapper);

        if (i >= 1) {
            ud = allOrderData(ud, request);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }

    // 获取所有订单
    @RequestMapping("/getAllOrderData")
    public DataModel getAllOrderData(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
//        ud = UserData.allUserData(ud, request);
        ud = allOrderData(ud, request);
        return ud;
    }

    // 获取所有用户订单的方法
    private DataModel allOrderData(DataModel ud, HttpServletRequest request) {
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);


        // 查总有多少条数据  select count(*) total from UserInformation;
        Long total = orderMapper.selectCount(null);


        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(Order::getStatus, 4)
                .last("limit " + limit + " offset " + skip);
        List<Order> limitMaps = orderMapper.selectList(queryWrapper);

        for (Order limitMap : limitMaps) {
            if (limitMap.getCreate_time() != null && !"".equals(limitMap.getCreate_time())) {
                limitMap.setCreate_time(limitMap.getCreate_time().substring(0, limitMap.getCreate_time().length() - 2));
            }
            if (limitMap.getPay_time() != null && !"".equals(limitMap.getPay_time())) {
                limitMap.setPay_time(limitMap.getPay_time().substring(0, limitMap.getPay_time().length() - 2));
            }
            if (limitMap.getEnd_time() != null && !"".equals(limitMap.getEnd_time())) {
                limitMap.setEnd_time(limitMap.getEnd_time().substring(0, limitMap.getEnd_time().length() - 2));
            }
            if (limitMap.getConsign_time() != null && !"".equals(limitMap.getConsign_time())) {
                limitMap.setConsign_time(limitMap.getConsign_time().substring(0, limitMap.getConsign_time().length() - 2));
            }
        }

        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(Math.toIntExact(total));
        } else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        return ud;
    }


    // 获取所有未发货订单
    @RequestMapping("/getAllUnshippedOrderData")
    public DataModel getAllUnshippedOrderData(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);


        // 查总有多少条数据  select count(*) total from UserInformation;
        Long total = orderMapper.selectCount(null);

        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        LambdaQueryWrapper<Order> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Order::getStatus, 2).last("limit " + limit + " offset " + skip);
        List<Order> limitMaps = orderMapper.selectList(lambdaQueryWrapper);

        for (Order limitMap : limitMaps) {
            if (limitMap.getCreate_time() != null && !"".equals(limitMap.getCreate_time())) {
                limitMap.setCreate_time(limitMap.getCreate_time().substring(0, limitMap.getCreate_time().length() - 2));
            }
            if (limitMap.getPay_time() != null && !"".equals(limitMap.getPay_time())) {
                limitMap.setPay_time(limitMap.getPay_time().substring(0, limitMap.getPay_time().length() - 2));
            }
            if (limitMap.getEnd_time() != null && !"".equals(limitMap.getEnd_time())) {
                limitMap.setEnd_time(limitMap.getEnd_time().substring(0, limitMap.getEnd_time().length() - 2));
            }
            if (limitMap.getConsign_time() != null && !"".equals(limitMap.getConsign_time())) {
                limitMap.setConsign_time(limitMap.getConsign_time().substring(0, limitMap.getConsign_time().length() - 2));
            }
        }

        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(Math.toIntExact(total));
        } else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        return ud;
    }

    @RequestMapping("/getAllrRmovedOorderData")
    public DataModel getAllrRmovedOorderData(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);


        // 查总有多少条数据  select count(*) total from UserInformation;
        Long total = orderMapper.selectCount(null);

        // 查询指定条数  select * from UserInformation limit 5 offset 3;

        LambdaQueryWrapper<Order> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Order::getStatus, 5).last("limit " + limit + " offset " + skip);
        List<Order> limitMaps = orderMapper.selectList(lambdaQueryWrapper);


        for (Order limitMap : limitMaps) {
            if (limitMap.getCreate_time() != null && !"".equals(limitMap.getCreate_time())) {
                limitMap.setCreate_time(limitMap.getCreate_time().substring(0, limitMap.getCreate_time().length() - 2));
            }
            if (limitMap.getPay_time() != null && !"".equals(limitMap.getPay_time())) {
                limitMap.setPay_time(limitMap.getPay_time().substring(0, limitMap.getPay_time().length() - 2));
            }
            if (limitMap.getEnd_time() != null && !"".equals(limitMap.getEnd_time())) {
                limitMap.setEnd_time(limitMap.getEnd_time().substring(0, limitMap.getEnd_time().length() - 2));
            }
            if (limitMap.getConsign_time() != null && !"".equals(limitMap.getConsign_time())) {
                limitMap.setConsign_time(limitMap.getConsign_time().substring(0, limitMap.getConsign_time().length() - 2));
            }
        }

        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(Math.toIntExact(total));
        } else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        return ud;
    }


    @GetMapping("/getUserInformationByOrderId")
    public UserInformation getUserInformationByOrderId(@RequestParam("orderId") Integer orderId){
        return orderMapper.getUserInformationByOrderId(orderId);
    }

    @GetMapping("/getOrderDetailsByOrderId")
    public List<OrderDetail> getOrderDetailsByOrderId(@RequestParam("orderId") Integer orderId){
        LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OrderDetail::getOrder_id,orderId);
        return orderDetailMapper.selectList(lambdaQueryWrapper);
    }
}
