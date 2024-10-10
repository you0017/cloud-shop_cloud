package com.yc.bean;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yc.mapper.ItemMapper;
import com.yc.utils.JmsMessageProducer;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.TimerTask;

@Data
@Component
public class MyWarnThread extends TimerTask {
    private String place;  // 预警位置
    private String warn_name;
    private String qqMail;
//    private String startTime = request.getParameter("start_time");  // 预警开始时间
//    private String week = request.getParameter("week");  // 预警周几
//    private String specific_time = request.getParameter("specific_time");  // 具体时间


    @Autowired
    private JmsMessageProducer jmsMessageProducer;
    @Autowired
    private ItemMapper itemMapper;

    public MyWarnThread() {
    }

    public MyWarnThread(String place, String warn_name, String qqMail) {
        this.place = place;
        this.warn_name = warn_name;
        this.qqMail = qqMail;
    }

    public void set(String place, String warn_name, String qqMail) {
        this.place = place;
        this.warn_name = warn_name;
        this.qqMail = qqMail;
    }

    @Override
    public void run() {

        // 测试
//        String sql = "select name from item where stock < stock_down";
//        List<Map<String, Object>> maps = dbHelper.select(sql);
//        String temp = "";
//        if (maps != null && maps.size() > 0) {
//            for (Map<String, Object> map : maps) {
//                temp += map.get("name") + "     ";
//            }
//        }
//        System.out.println(temp);
        // 正式

        System.out.println("开始执行");

        QueryWrapper<Item> wrapper = new QueryWrapper<>();
        wrapper.select("name").lt("stock", "stock_down");
        List<Item> maps = itemMapper.selectList(wrapper);

        String temp = "";
        if (maps != null && maps.size() > 0) {
            for (int i = 0; i < maps.size() - 1; i++) {
                temp += maps.get(i).getName() + " , ";
            }
//                for (Map<String, Object> map : maps) {
//                    temp += map.get("name") + "  ";
//                }
            String o = (String) maps.get(maps.size() - 1).getName();
            temp += o;
            jmsMessageProducer.warningMessage(" 尊敬的" + warn_name + "老板，您位于 '" + place + "' 的仓库商品:" + temp + "的库存不足，请及时补货！", qqMail);
        } else {
            jmsMessageProducer.warningMessage(" 尊敬的" + warn_name + "老板，您位于 '" + place + "' 的商品库存充足！", qqMail);
        }

        System.out.println("任务执行了");
    }
}
