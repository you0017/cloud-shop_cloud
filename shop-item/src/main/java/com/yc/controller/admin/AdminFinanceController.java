package com.yc.controller.admin;




import com.yc.bean.DataModel;
import com.yc.bean.Sale;
import com.yc.mapper.ItemMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
//@WebServlet("/admin/finance.action")
@RestController
@RequestMapping("/finance/admin")
public class AdminFinanceController {
    @Autowired
    private ItemMapper itemMapper;

    // 按时间段查询销售
// 年份来算    SELECT item.name, YEAR(`order`.end_time) AS year, SUM(order_detail.num) AS total_sales FROM order_detail JOIN `order` ON order_detail.order_id = `order`.id JOIN item ON order_detail.item_id = item.id WHERE `order`.end_time IS NOT NULL GROUP BY item.id, YEAR(`order`.end_time);
// SELECT YEAR(`order`.end_time) AS year, item.name, item.price, SUM(order_detail.num) AS total_sales, SUM(order_detail.num * order_detail.price) AS total_revenue FROM order_detail JOIN `order` ON order_detail.order_id = `order`.id JOIN item ON order_detail.item_id = item.id WHERE `order`.end_time IS NOT NULL GROUP BY YEAR(`order`.end_time), item.id;
    @RequestMapping("/getSaleDataByTime")
    public DataModel getSaleDataByTime(HttpServletRequest request, HttpServletResponse response) {
        String timeYear = request.getParameter("timeYear");
        String timeMonth = request.getParameter("timeMonth");
        String timeDay = request.getParameter("timeDay");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        // 查询指定时间段的数据
        DataModel ud = new DataModel();
        if ( timeYear!=null){
            String sql = "SELECT item.name, YEAR(`order`.end_time) AS year, " +
                    "SUM(order_detail.num) AS total_sales FROM order_detail " +
                    "JOIN `order` ON order_detail.order_id = `order`.id " +
                    "JOIN item ON order_detail.item_id = item.id " +
                    "WHERE `order`.end_time IS NOT NULL GROUP BY item.id, YEAR(`order`.end_time)";
        }
//        try {
//            ud = getAllrSaleDataByTime(ud, startTime, endTime);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
       return ud;
    }

    // 计算总的销售   不分时间段
    @RequestMapping("/getSaleData")
    public DataModel getSaleData(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        try {
            ud = getAllrSaleData(ud, request);
        }catch (Exception e) {
            e.printStackTrace();
        }
       return ud;
    }

    public DataModel getAllrSaleData( DataModel ud, HttpServletRequest request) {
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        // 查总有多少条数据  select count(*) total from UserInformation;
        Long total = itemMapper.selectCount(null);

        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        // SELECT `name`, price, price * sold AS total_price, sold  FROM item;
        List<Sale> limitMaps = itemMapper.getAllrSaleData(limit, skip);

        if ( limitMaps!=null && limitMaps.size()>0 ) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(Math.toIntExact(total));
        }else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        return ud;
    }

}
