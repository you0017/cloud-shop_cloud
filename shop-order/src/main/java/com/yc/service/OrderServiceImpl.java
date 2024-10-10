package com.yc.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yc.api.CouponClient;
import com.yc.api.ItemClient;
import com.yc.bean.*;
import com.yc.context.BaseContext;
import com.yc.mapper.OrderDetailMapper;
import com.yc.mapper.OrderMapper;
import com.yc.mapper.ReturnOrdersMapper;
import com.yc.model.JsonModel;
import com.yc.service.OrderService;
import com.yc.utils.YcConstants;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
@Transactional
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CouponClient couponClient;
    @Autowired
    private ReturnOrdersMapper returnOrdersMapper;

    /**
     * 取消订单
     *
     * @param reason
     * @param id
     */
    @GlobalTransactional//分布式事务
    @Override
    public void cancel(String reason, String id) {
        //订单改取消
        this.lambdaUpdate().eq(Order::getId, id).set(Order::getStatus, 8).set(Order::getReason, reason).update();
        //改商品数量
        //select item_id from order_detail where order_id=?
        //查这个订单有哪些商品，订单详细表
        LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OrderDetail::getOrder_id, id).select(OrderDetail::getItem_id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(lambdaQueryWrapper);
        //加了分布式事务
        //回退商品数量
        try {
            itemClient.fallback(orderDetails);
        } catch (Exception e) {
            throw new RuntimeException("回退商品数量失败");
        }

    }

    @Override
    public List<OrderList> getOrder(Integer pageNo, Integer pageSize) {
        String user_id = BaseContext.getCurrentId();
        int start = (pageNo - 1) * pageSize;
        //该用户所有订单
        //String sql = "select id,total_fee,status,create_time,address,mobile,contact,actual_payment,shipping_fee from `order` where user_id=? and status != 7 order by id desc limit "+pagesize+" offset "+start;
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUser_id, Integer.valueOf(user_id)).orderByDesc(Order::getId);

        Page<Order> page = orderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<Order> records = page.getRecords(); //订单
        List<Integer> ids = records.stream().map(Order::getId).toList();

        //订单集合
        List<OrderList> orderLists = new ArrayList<>();
        for (Order order : records) {
            //主订单
            OrderList orderList = new OrderList();
            BeanUtils.copyProperties(order, orderList);
            orderLists.add(orderList);

            //订单详情
            LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(OrderDetail::getOrder_id, order.getId());
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailWrapper);//订单详细项
            orderList.setOrderDetailList(orderDetails);
        }


        return orderLists;
    }

    /**
     * 支付
     */
    @Override
    @GlobalTransactional//分布式事务
    public Order pay(OrderVO orderVO) {
        String user_id = BaseContext.getCurrentId();
        //购物车 id <-> 数量
        Map<String, String> map = redisTemplate.opsForHash().entries(YcConstants.CARTITEMS + user_id);

        /**
         * 目前是有了购物车的id及其数量，现在要遍历每个id进行查询，记录一下哪些id能够被买，如果stock<=0就不能买了
         * 记录id是方便在购物车中删除
         * 还有就是如果购物车中存在商品数量不足，就抛异常，不要继续执行，提醒前端xx商品不够了，要移除购物车才能进行支付
         * 还有就是考虑下架问题
         */

        Double total = 0.0;
        // 取出map里面所有的键
        List<String> ids = new ArrayList<>(map.keySet());
        List<Item> items = itemClient.getByIds(ids);


        if (items == null || items.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        // 遍历购物车，算总价，并且封装到items里面，顺便看库存够不够
        for (Item item : items) {
            if (item.getStock() <= 0 || item.getStatus() == 0) {
                throw new RuntimeException(item.getName() + "库存不足或已下架");
            }
            total += item.getPrice() * Double.parseDouble(map.get(item.getId()));
        }


        Order order = Order.builder().total_fee(total)
                .id(orderVO.getId())
                .user_id(Integer.valueOf(user_id))
                .status(2)//已支付，因为沙箱后面我控制不了，这里点了提交就算支付。        未支付
                .address(orderVO.getProvince() + orderVO.getCity() + orderVO.getTown() + orderVO.getStreet())
                .contact(orderVO.getContact())
                .mobile(orderVO.getMobile())
                .shipping_fee(Double.valueOf(orderVO.getFreight()))
                .coupon_id(0)//券id  默认为0  insert方便一点，懒得判断了
                .build();

        Integer coupon_id = orderVO.getCoupon_id();

        Coupon coupon = null;
        if (coupon_id != null) {
            //说明用了优惠券 //找到这张优惠券详情
            try {
                coupon = couponClient.checkCouponByOrder(String.valueOf(coupon_id));
            } catch (Exception e) {
                throw new RuntimeException("优惠券不存在");
            }

            Double discount = coupon.getDiscount();
            total = total + discount;
            order.setCoupon_id(Integer.valueOf(coupon_id));//券id
        }
        order.setActual_payment(total);//折后价格


        Integer order_id = null;
        //订单，购物车，和需要的商品
        order_id = order(order, map, items, coupon);//返回订单的id
        order.setId(order_id);//订单id

        //并且把用的券改状态如果用了
        if (coupon_id != null && !order.getCoupon_id().equals(0)) {
            Integer code = couponClient.useCoupon(user_id, coupon_id).getCode();
            if (code.equals(0)) {
                throw new RuntimeException("优惠券使用失败");
            }
        }

        //删除购物车
        redisTemplate.delete(YcConstants.CARTITEMS + user_id);

        return order;
    }

    @Override
    public boolean ret(String op, String itemId, String orderId, String reason, String trackingCompany, String trackingNumber, String returnId) {
        String user_id = BaseContext.getCurrentId();

        int i = 0;
        if (op.equals("1")) {//退货
            //查订单的相关详细订单
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrder_id, orderId)
                    .eq(OrderDetail::getItem_id, itemId);
            //查到该订单商品有关的详细订单
            List<OrderDetail> select = orderDetailMapper.selectList(wrapper);

            //退货表插入数据
            ReturnOrder returnOrder = new ReturnOrder();
            returnOrder.setOrder_id(Integer.valueOf(orderId));
            returnOrder.setCustomer_id(Integer.valueOf(user_id));
            returnOrder.setProduct_id(Integer.valueOf(itemId));
            returnOrder.setReturn_reason(reason);
            returnOrder.setReturn_quantity(select.get(0).getNum());
            returnOrder.setReturn_status(String.valueOf(1)); // 根据需求设置状态
            returnOrder.setReturn_date(LocalDateTime.now().toString());
            returnOrder.setRefund_amount(select.get(0).getActual_payment() * select.get(0).getNum());
            returnOrder.setProduct_name(select.get(0).getName());
            returnOrder.setBack_type(String.valueOf(2)); // 根据需求设置返回类型

            i = returnOrdersMapper.insert(returnOrder);

            //更改详细表这个商品的状态
            LambdaUpdateWrapper<OrderDetail> wrapper1 = new LambdaUpdateWrapper<>();
            wrapper1.set(OrderDetail::getReturn_status, 2)
                    .eq(OrderDetail::getOrder_id, orderId)
                    .eq(OrderDetail::getItem_id, itemId);
            orderDetailMapper.update(null, wrapper1);

        } else if (op.equals("3")) {//仅退款
            //查订单的相关详细订单
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrder_id, orderId)
                    .eq(OrderDetail::getItem_id, itemId);
            //查到该订单商品有关的详细订单
            List<OrderDetail> select = orderDetailMapper.selectList(wrapper);

            //退货表插入数据
            ReturnOrder returnOrder = new ReturnOrder();
            returnOrder.setOrder_id(Integer.valueOf(orderId));
            returnOrder.setCustomer_id(Integer.valueOf(user_id));
            returnOrder.setProduct_id(Integer.valueOf(itemId));
            returnOrder.setReturn_reason(reason);
            returnOrder.setReturn_quantity(select.get(0).getNum());
            returnOrder.setReturn_status(String.valueOf(1)); // 根据需求设置状态
            returnOrder.setReturn_date(LocalDateTime.now().toString());
            returnOrder.setRefund_amount(select.get(0).getActual_payment() * select.get(0).getNum());
            returnOrder.setProduct_name(select.get(0).getName());
            returnOrder.setBack_type(String.valueOf(1)); // 根据需求设置返回类型
            i = returnOrdersMapper.insert(returnOrder);


            //更改详细表这个商品的状态
            LambdaUpdateWrapper<OrderDetail> wrapper1 = new LambdaUpdateWrapper<>();
            wrapper1.set(OrderDetail::getReturn_status, 2)
                    .eq(OrderDetail::getOrder_id, orderId)
                    .eq(OrderDetail::getItem_id, itemId);
            orderDetailMapper.update(null, wrapper1);
        } else if (op.equals("2")) {
            //退货填单号和公司，这是已经同意了
            LambdaUpdateWrapper<ReturnOrder> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(ReturnOrder::getTracking_number, trackingNumber)
                    .set(ReturnOrder::getTracking_company, trackingCompany)
                    .set(ReturnOrder::getReturn_status, 3)
                    .eq(ReturnOrder::getReturn_id, returnId);
            i = returnOrdersMapper.update(null, updateWrapper);
        }
        //return_status 1申请 2申请通过 3快递审核 4通过 5不通过
        if (i > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断用户是否购买了该商品
     */
    @Override
    public boolean hasUserPurchasedProduct(String id, String userId) {

        List<Map<String, Object>> orderDetail = orderMapper.hasUserPurchasedProduct(id,userId);
        if (orderDetail != null && orderDetail.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 订单详情问题
     */
    public int order(Order order, Map<String, String> map, List<Item> items, Coupon coupon) {

        //插入订单表
        boolean result = this.save(order);

        //取自动生成的id
        int roid = order.getId();


        Double discount = 0.0;
        //用过券
        if (!order.getCoupon_id().equals(Integer.valueOf("0"))) {
            discount = coupon.getDiscount();//得到折扣价格
        }

        //循环所有的订单项，添加到order_detail表
        if (items == null || items.size() <= 0) {
            throw new RuntimeException("购物车为空");
        }

        List<OrderDetail> list = new ArrayList<>();
        for (Item item : items) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .order_id(roid)//订单id
                    .item_id(Integer.valueOf(item.getId()))//商品id
                    .num(Integer.valueOf(map.get(item.getId())))//商品数量
                    .name(item.getName())//商品名
                    .spec(item.getSpec())//商品描述
                    .price(item.getPrice())//商品价格
                    .image(item.getImage())//商品图片
                    .create_time(LocalDateTime.now().toString())//时间
                    .update_time(LocalDateTime.now().toString())
                    .build();
            if (!order.getCoupon_id().equals("0")) {
                //这是用过优惠券

                // item.getPrice() / order.getTotal_fee() 保留两位小数的优化
                Double v = Double.parseDouble(String.format("%.2f", item.getPrice() / order.getTotal_fee()));

                orderDetail.setActual_payment(item.getPrice() + v * discount);//实际价格
            } else {
                //没用过券
                orderDetail.setActual_payment(item.getPrice());//实际价格
            }

            list.add(orderDetail);
        }
        //插入所有详情列
        orderDetailMapper.insertOrUpdate(list);

        return roid;
    }
}
