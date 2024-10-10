package com.yc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yc.bean.*;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface OrderMapper extends BaseMapper<Order> {

    @Select("select * from shoporder left join order_detail on shoporder.id=order_detail.order_id where order_detail.item_id=#{id} and shoporder.user_id=#{userId}")
    List<Map<String, Object>> hasUserPurchasedProduct(String id, String userId);

    @Select("SELECT DATE(create_time) AS order_time, COUNT(*) AS order_sum, SUM(total_fee) AS order_sales " +
            "FROM shoporder WHERE create_time >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY) " +
            "GROUP BY DATE(create_time)")
    public List<AdminOrderGrap> getInitOrderGraph();

    @Select("SELECT i.category as category, SUM(od.num) AS total_sales, ROUND(SUM(od.num * od.actual_payment), 2) AS total_revenue " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id GROUP BY i.category")
    public List<GraphSale> getInitGraph();

    @Select("SELECT i.category as category, SUM(od.num) AS total_sales, ROUND(SUM(od.num * od.actual_payment), 2) AS total_revenue " +
            "FROM order_detail od JOIN item i ON od.item_id = i.id " +
            "WHERE DATE(od.create_time) = CURDATE() GROUP BY i.category")
    public List<GraphSale> getTodayGraph();

    @Select("SELECT item.category, SUM(order_detail.num) AS total_sales, SUM(order_detail.price * order_detail.num) AS total_revenue " +
            "FROM item JOIN order_detail " +
            "ON item.id = order_detail.item_id JOIN shoporder " +
            "ON order_detail.order_id = shoporder.id " +
            "WHERE DATE(shoporder.create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) GROUP BY item.category")
    public List<GraphSale> getYesterdayGraph();

    @Select("SELECT item.category, SUM(order_detail.num) AS total_sales, SUM(order_detail.price * order_detail.num) AS total_revenue " +
            "FROM item JOIN order_detail " +
            "ON item.id = order_detail.item_id JOIN shoporder " +
            "ON order_detail.order_id = shoporder.id " +
            "WHERE MONTH(shoporder.create_time) = MONTH(CURRENT_DATE) " +
            "AND YEAR(shoporder.create_time) = YEAR(CURRENT_DATE) GROUP BY item.category")
    public List<GraphSale> getMonthGraph();

    @Select("SELECT item.category, SUM(order_detail.num * order_detail.price) AS total_revenue, SUM(order_detail.num) AS total_sales " +
            "FROM item INNER JOIN order_detail ON item.id = order_detail.item_id INNER JOIN shoporder ON order_detail.order_id = shoporder.id " +
            "WHERE YEAR(shoporder.create_time) = YEAR(CURRENT_DATE()) GROUP BY item.category")
    public List<GraphSale> getYearGraph();

    @Select("SELECT item.category, SUM(order_detail.num * order_detail.price) AS total_revenue, SUM(order_detail.num) AS total_sales " +
            "FROM item INNER " +
            "JOIN order_detail ON item.id = order_detail.item_id INNER " +
            "JOIN shoporder ON order_detail.order_id = shoporder.id " +
            "WHERE YEAR(shoporder.create_time) = #{year} AND MONTH(shoporder.create_time) = #{month} GROUP BY item.category")
    public List<GraphSale> getMonthYearGraph(String year, String month);

    @Select("SELECT COUNT(*) as total FROM (" +
            "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id GROUP BY i.id " +
            ") AS subquery")
    public List<Map<String, Object>> getGoodsTop1();

    @Select("SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id GROUP BY i.id ORDER BY sold_quantity DESC limit #{limit} offset #{skip}")
    public List<AdminTop> getGoodsTop2(int limit, int skip);

    @Select("SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id " +
            "WHERE DATE(od.create_time) = CURDATE() GROUP BY i.id ORDER BY sold_quantity DESC")
    public List<AdminTop> getTodayTop1();

    @Select("SELECT COUNT(*) as total FROM (" +
            "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id " +
            "WHERE DATE(od.create_time) = CURDATE() GROUP BY i.id"
            + ") AS subquery")
    public List<Map<String, Object>> getTodayTop2();

    @Select("SELECT COUNT(*) as total FROM (" +
            "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id " +
            "WHERE DATE(od.create_time) = CURDATE() - 1 GROUP BY i.id ORDER BY sold_quantity DESC"
            + ") AS subquery")
    public List<Map<String, Object>> yesterdayTop1();

    @Select("SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id " +
            "WHERE DATE(od.create_time) = CURDATE() - 1 GROUP BY i.id ORDER BY sold_quantity DESC")
    public List<AdminTop> yesterdayTop2();

    @Select("SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id " +
            "WHERE MONTH(od.create_time) = MONTH(CURRENT_DATE) AND YEAR(od.create_time) = YEAR(CURRENT_DATE) " +
            "GROUP BY i.id ORDER BY sold_quantity DESC")
    public List<AdminTop> monthTop1();

    @Select("SELECT COUNT(*) as total FROM (" +
            "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id " +
            "WHERE MONTH(od.create_time) = MONTH(CURRENT_DATE) AND YEAR(od.create_time) = YEAR(CURRENT_DATE) " +
            "GROUP BY i.id"
            + ") AS subquery")
    public List<Map<String, Object>> monthTop2();

    @Select("SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id " +
            "WHERE MONTH(od.create_time) = MONTH(CURRENT_DATE) AND YEAR(od.create_time) = YEAR(CURRENT_DATE) " +
            "GROUP BY i.id ORDER BY sold_quantity DESC")
    public List<AdminTop> yearTop1();

    @Select("SELECT COUNT(*) as total FROM (" +
            "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
            "FROM item i JOIN order_detail od ON i.id = od.item_id " +
            "WHERE MONTH(od.create_time) = MONTH(CURRENT_DATE) AND YEAR(od.create_time) = YEAR(CURRENT_DATE) " +
            "GROUP BY i.id"
            + ") AS subquery")
    public List<Map<String, Object>> yearTop2();

    @Select("select * from userinformation where id=(SELECT user_id from shoporder where id=#{orderId})")
    public UserInformation getUserInformationByOrderId(Integer orderId);

}
