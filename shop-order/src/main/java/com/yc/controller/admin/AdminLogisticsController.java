package com.yc.controller.admin;




import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.bean.DataModel;
import com.yc.bean.Logistics;
import com.yc.mapper.LogisticsMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;


//@WebServlet("/admin/logistics.action")
@RestController
@RequestMapping("/logistics/admin")
public class AdminLogisticsController {

    @Autowired
    private LogisticsMapper logisticsMapper;
    @RequestMapping("/getAllLogisticsData")
    public DataModel getAllLogisticsData(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);


        LambdaQueryWrapper<Logistics> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .orderByDesc(Logistics::getShipping_date)
                .last("LIMIT " + limit + " OFFSET " + skip);
        List<Logistics> select = logisticsMapper.selectList(lambdaQueryWrapper);

        Long total = logisticsMapper.selectCount(null);

        if (select!=null && select.size()>0) {
            ud.setData(select);
            ud.setCode(0);
            ud.setCount(Math.toIntExact(total));
        }else {
            ud.setCode(1);
            ud.setMsg("没有数据");
        }
        return ud;
    }
}
