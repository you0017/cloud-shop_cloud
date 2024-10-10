package com.yc.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.bean.AdminOrderGrap;
import com.yc.bean.AdminTop;
import com.yc.bean.DataModel;
import com.yc.bean.GraphSale;
import com.yc.mapper.OrderMapper;
import com.yc.model.JsonModel;
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

//@WebServlet("/admin/graph.action")
@RestController
@RequestMapping("/graph/admin")
public class AdminGraphController {
    @Autowired
    private OrderMapper orderMapper;

    // 订单的数据
    @RequestMapping("/getInitOrderGraph")
    public JsonModel getInitOrderGraph(HttpServletRequest request, HttpServletResponse response) {
        JsonModel jsonModel = new JsonModel();

        List<AdminOrderGrap> select = orderMapper.getInitOrderGraph();

        ArrayList end = new ArrayList<>();
        ArrayList<Integer> order_sum = new ArrayList<>();   // 订单量
        ArrayList<Double> order_sales = new ArrayList<>();  // 销售额
        ArrayList<String> order_time = new ArrayList<>();  // 时间
        for (AdminOrderGrap adminOrderGrap : select) {
            order_time.add(adminOrderGrap.getOrder_time().toString());
            order_sum.add(adminOrderGrap.getOrder_sum().intValue());
            order_sales.add(adminOrderGrap.getOrder_sales());
        }
        end.add(order_time);
        end.add(order_sum);
        end.add(order_sales);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        return jsonModel;
    }


    // 下面的商品销售的数据
    // 年度top
    @RequestMapping("/yearTop")
    public DataModel yearTop(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();

        List<AdminTop> select = orderMapper.yearTop1();
        List<Map<String, Object>> maps = orderMapper.yearTop2();
        int total = Integer.parseInt(maps.get(0).get("total").toString());

        ud.setCode(0);
        ud.setData(select);
        ud.setCount(total);
        return ud;

    }

    // 本月
    @RequestMapping("/getMonthTop")
    public DataModel monthTop(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();

        List<AdminTop> select = orderMapper.monthTop1();
        List<Map<String, Object>> maps = orderMapper.monthTop2();
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        ud.setCode(0);
        ud.setCount(total);
        ud.setData(select);
        return ud;
    }

    // 昨日销量top
    @RequestMapping("/getYesterdayTop")
    public DataModel yesterdayTop(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();

        List<Map<String, Object>> maps = orderMapper.yesterdayTop1();
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        List<AdminTop> select = orderMapper.yesterdayTop2();
        ud.setCode(0);
        ud.setCount(total);
        ud.setData(select);
        return ud;
    }

    // 今日销量top
    @RequestMapping("/getTodayTop")
    public DataModel getTodayTop(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        List<AdminTop> select = orderMapper.getTodayTop1();
        List<Map<String, Object>> maps = orderMapper.getTodayTop2();
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        ud.setCode(0);
        ud.setCount(total);
        ud.setData(select);
        return ud;
    }

    // 销量前30
    @RequestMapping("/getGoodsTop")
    public DataModel getGoodsTop(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);

        List<Map<String, Object>> maps = orderMapper.getGoodsTop1();
        int total = Integer.parseInt(maps.get(0).get("total").toString());

        List<AdminTop> select = orderMapper.getGoodsTop2(limit, skip);

        ud.setCode(0);
        ud.setCount(total);
        ud.setData(select);
        return ud;
    }

    // 指定年月的数据
    @RequestMapping("/getMonthYearGraph")
    public JsonModel getMonthYearGraph(HttpServletRequest request, HttpServletResponse response) {
        JsonModel jm = new JsonModel();
        String month = request.getParameter("month");
        String year = request.getParameter("year");

        List<GraphSale> select = orderMapper.getMonthYearGraph(year, month);
        ArrayList end = new ArrayList<>();
        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jm.setObj(end);
        jm.setCode(1);
        jm.setError("");
        return jm;
    }

    // 今年的数据
    @RequestMapping("/getYearGraph")
    public JsonModel getYearGraph(HttpServletRequest request, HttpServletResponse response) {
        JsonModel jsonModel = new JsonModel();

        ArrayList end = new ArrayList<>();
        List<GraphSale> select = orderMapper.getYearGraph();

        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        return jsonModel;
    }

    // 这个月的数据
    @RequestMapping("/getMonthGraph")
    public JsonModel getMonthGraph(HttpServletRequest request, HttpServletResponse response) {
        JsonModel jsonModel = new JsonModel();
        ArrayList end = new ArrayList<>();
        List<GraphSale> select =orderMapper.getMonthGraph();
        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        return jsonModel;
    }

    // 昨天的数据
    @RequestMapping("/getYesterdayGraph")
    public JsonModel getYesterdayGraph(HttpServletRequest request, HttpServletResponse response) {
        JsonModel jsonModel = new JsonModel();

        ArrayList end = new ArrayList<>();
        List<GraphSale> select = orderMapper.getYesterdayGraph();

        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
       return jsonModel;
    }


    // 获取今日销售情况
    @RequestMapping("/getTodayGraph")
    public JsonModel getTodayGraph(HttpServletRequest request, HttpServletResponse response) {
        JsonModel jsonModel = new JsonModel();

        ArrayList end = new ArrayList<>();
        List<GraphSale> select = orderMapper.getTodayGraph();

        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        return jsonModel;
    }

    // 初始化图表
    @RequestMapping("/getInitGraph")
    public JsonModel getInitGraph(HttpServletRequest request, HttpServletResponse response) {
        JsonModel jsonModel = new JsonModel();

//        String sql = "select category, SUM(sold) as sold from item GROUP BY category";
        ArrayList end = new ArrayList<>();
        List<GraphSale> select = orderMapper.getInitGraph();

        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue());
            total_sales.add(graph.getTotal_sales());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        return jsonModel;
    }
}
