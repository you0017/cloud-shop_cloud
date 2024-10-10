package com.yc.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yc.bean.DataModel;
import com.yc.bean.Item;
import com.yc.bean.MyWarnThread;
import com.yc.mapper.ItemMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

//@WebServlet("/admin/inventory.action")
@RestController
@RequestMapping("/inventory/admin")
public class AdminInventoryController {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private MyWarnThread myWarnThread;
    // 预警信息
    @RequestMapping("/setTime")
    public DataModel setTime(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        System.out.println("there");
        String place = request.getParameter("place");  // 预警位置
        String warn_name = request.getParameter("warn_name"); // 通知谁
        String qqMail = request.getParameter("qq_mail"); // qq邮箱
        String startTime = request.getParameter("start_time");  // 预警开始时间
        String week = request.getParameter("week");  // 预警周几
        int week_num = Integer.parseInt(week);  // 周几的int型
        String specific_time = request.getParameter("specific_time");  // 具体时间
        // 解析日期字符串
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null;   // 预警开始时间
            startDate = formatter.parse(startTime);   // 预警开始时间
            // 计算距离下一个周二的时间间隔
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            // 如果不是我要的周几，则一直循环，直到找到我需要的周几
            while (calendar.get(Calendar.DAY_OF_WEEK) != week_num) {
//                System.out.println(calendar.getTime());
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            LocalTime time = LocalTime.parse(specific_time);
            // 具体定时的毫秒数
            long milliseconds = ChronoUnit.MILLIS.between(LocalTime.MIDNIGHT, time);
            calendar.add(Calendar.MILLISECOND, (int) milliseconds);
            Date nextTuesday = calendar.getTime();
            Timer timer = new Timer();
            long period = 7 * 24 * 60 * 60 * 1000; // 一周的毫秒数

            myWarnThread.set(place, warn_name, qqMail);   // 任务线程
            timer.schedule(myWarnThread, nextTuesday, period);

            ud.setCode(0);
            ud.setMsg("设置成功");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return ud;

    }


    // 批量预警
    @RequestMapping("/batchWarn")
    public DataModel batchWarn(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String form = request.getParameter("form");
        String form1_stock_down = request.getParameter("form1_stock_down");
        String form1_stock_up = request.getParameter("form1_stock_up");
        String form2_stock_down = request.getParameter("form2_stock_down");
        String form2_stock_up = request.getParameter("form2_stock_up");
        String idsStr = request.getParameter("idsStr");  // 商品编码
        String[] strArray = idsStr.split(",");

        LambdaUpdateWrapper<Item> lambdaUpdateWrapper = null;
        if ("form1".equals(form)) {  // 证明是一件固定预警
            lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper
                    .set(Item::getStock_down, form1_stock_down)
                    .set(Item::getStock_up, form1_stock_up)
                    .set(Item::getWarning_value_status, 1)
                    .in(Item::getId, strArray);
        } else if ("form2".equals(form)) {  // 证明是一键动态预警
            lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper
                    .set(Item::getStock_down, 200)
                    .set(Item::getStock_up, 500)
                    .set(Item::getWarning_value_status, 2)
                    .in(Item::getId, strArray);
        }

        int result = itemMapper.update(null, lambdaUpdateWrapper);
        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("预警成功");
        } else {
            ud.setCode(1);
            ud.setMsg("预警失败");
        }
        return ud;
    }

    // 一键预警
    @RequestMapping("/editAllLimit")
    public DataModel editAllLimit(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String form = request.getParameter("form");
        String id = request.getParameter("id");  // 商品编码
        String form1_stock_down = request.getParameter("form1_stock_down");
        String form1_stock_up = request.getParameter("form1_stock_up");
        String form2_stock_down = request.getParameter("form2_stock_down");
        String form2_stock_up = request.getParameter("form2_stock_up");
        if ("form1".equals(form)) {  // 证明是一件固定预警
            LambdaUpdateWrapper<Item> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(Item::getWarning_value_status, 1)
                    .set(Item::getStock_down, Integer.parseInt(form1_stock_down))
                    .set(Item::getStock_up, Integer.parseInt(form1_stock_up));
            int result = itemMapper.update(null, lambdaUpdateWrapper);

            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("一键设置成功");
            } else {
                ud.setCode(1);
                ud.setMsg("一键设置失败");
            }
        } else if ("form2".equals(form)) {  // 证明是一键动态预警
            LambdaUpdateWrapper<Item> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(Item::getWarning_value_status, 2)
                    .set(Item::getStock_down, 200)
                    .set(Item::getStock_up, 500);
            int result = itemMapper.update(null, lambdaUpdateWrapper);

            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("一键设置成功");
            } else {
                ud.setCode(1);
                ud.setMsg("一键设置失败");
            }
        } else {
            ud.setCode(1);
            ud.setMsg("一键设置失败");
        }
        return ud;
    }

    // 动态监控库存
    @RequestMapping("/realLook")
    public DataModel realLook(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("realLook");
        DataModel ud = new DataModel();

        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("warning_value", "stock");
        List<Item> items = itemMapper.selectList(queryWrapper);

        if (items.size() >= 1) {
            ud.setCode(0);
            ud.setMsg("库存预警");
        } else {
            ud.setCode(1);
            ud.setMsg("库存正常");
        }
        return ud;

    }


    // 设置固定预警上下限
    @RequestMapping("/editLimit")
    public DataModel editLimit(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("setLimit");
        HttpSession session = request.getSession();
        DataModel ud = new DataModel();
        String form = request.getParameter("form");
        String id = request.getParameter("id");  // 商品编码
        String form1_stock_down = request.getParameter("form1_stock_down");
        String form1_stock_up = request.getParameter("form1_stock_up");
        String form2_stock_down = request.getParameter("form2_stock_down");
        String form2_stock_up = request.getParameter("form2_stock_up");
        if ("form1".equals(form)) {  // 证明是固定预警
            LambdaUpdateWrapper<Item> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(Item::getWarning_value_status, 1)
                    .set(Item::getStock_down, Integer.parseInt(form1_stock_down))
                    .set(Item::getStock_up, Integer.parseInt(form1_stock_up))
                    .eq(Item::getId, id);
            int result = itemMapper.update(null, lambdaUpdateWrapper);

            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("更新成功");
            } else {
                ud.setCode(1);
                ud.setMsg("更新失败");
            }
        } else if ("form2".equals(form)) {  // 证明是动态预警
            LambdaUpdateWrapper<Item> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(Item::getWarning_value_status, 2)
                    .set(Item::getStock_down, 200)
                    .set(Item::getStock_up, 500)
                    .eq(Item::getId, id);
            int result = itemMapper.update(null, lambdaUpdateWrapper);

            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("更新成功");
            } else {
                ud.setCode(1);
                ud.setMsg("更新失败");
            }
        } else {
            ud.setCode(1);
            ud.setMsg("更新失败");
        }
        return ud;
    }


    // 更新库存|补货
    @RequestMapping("/replenishment")
    public DataModel replenishment(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("replenishment");
        DataModel ud = new DataModel();
        String stock = request.getParameter("stock");
        String name = request.getParameter("name");
        if (stock == null || "".equals(stock)) {
            ud.setCode(1);
            ud.setMsg("请输入库存");
            return ud;
        }

        LambdaUpdateWrapper<Item> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(Item::getStock, Integer.parseInt(stock)).eq(Item::getName, name);
        int result = itemMapper.update(null, updateWrapper);

        if (result > 0) {
            ud.setCode(0);
            ud.setMsg("更新成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }


    // 获取所有的缺货库存信息
    @RequestMapping("/getAllLackStockData")
    public DataModel getAllLackStockData(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("getAllLackStockData");
        DataModel ud = new DataModel();

        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);


        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("stock", "warning_value"); // stock < warning_value
        Long total = itemMapper.selectCount(queryWrapper);

        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("stock", "warning_value").last("limit "+limit+" offset "+skip);
        List<Item> limitMaps = itemMapper.selectList(queryWrapper);


        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
        } else {
            ud.setCode(1);
        }
        return ud;
    }


    // 设置预警值
    @RequestMapping("editWarning")
    public DataModel editWarning(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("editWarning");
        DataModel ud = new DataModel();
        String stock = request.getParameter("stock");
        String name = request.getParameter("name");
        String warning = request.getParameter("warning");
        if (warning == null || "".equals(warning)) {
            ud.setCode(1);
            ud.setMsg("请输入预警值");
            return ud;
        }

        LambdaUpdateWrapper<Item> db = new LambdaUpdateWrapper<>();
        db.set(Item::getWarning_value, Integer.parseInt(warning)).eq(Item::getName, name).eq(Item::getStock, stock);
        int result = itemMapper.update(null, db);


        if (result > 0) {
            ud.setCode(0);
            ud.setMsg("更新成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;

    }


    // 1.获取所有的库存信息
    @RequestMapping("/getAllStockData")
    public DataModel getAllStockData(HttpServletRequest request, HttpServletResponse response) {
//        System.out.println("getAllStockData");
        // 1.1 获取所有的库存信息
        DataModel ud = new DataModel();

        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);


        // 查总有多少条数据  select count(*) total from UserInformation;
        Long total = itemMapper.selectCount(null);
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.last("limit " + limit + " offset " + skip);
        List<Item> limitMaps = itemMapper.selectList(queryWrapper);

        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(Math.toIntExact(total));
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }


}
