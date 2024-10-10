package com.yc.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yc.bean.DataModel;
import com.yc.bean.DataRecord;
import com.yc.mapper.DataRecordMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 设置平台的属性
//@WebServlet("/admin/frontEdit.action")
@RestController
@RequestMapping("/frontEdit/admin")
public class AdminFrontController {
    @Autowired
    private DataRecordMapper dataRecordMapper;

    // 数据字典的选择下拉框
    @RequestMapping("/getKindName")
    public DataModel getKindName(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String value = request.getParameter("value");
        String data = request.getParameter("data");
        String name = request.getParameter("name");

        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        // 查总有多少条数据J  select count(*) total from UserInformation;
        Long total = dataRecordMapper.selectCount(null);


        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        LambdaQueryWrapper<DataRecord> lqw = Wrappers.lambdaQuery();
        lqw.last("limit " + limit + " offset " + skip).eq(DataRecord::getRecorde_name, name);
        List<DataRecord> limitMaps = dataRecordMapper.selectList(lqw);
//        List<UserInformation> select = db.select(UserInformation.class, "select * from UserInformation");
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

    // getAllKind
    @RequestMapping("/getAllKind")
    public DataModel getAllKind(HttpServletRequest request, HttpServletResponse response) {
        // 创建 QueryWrapper 对象
        QueryWrapper<DataRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT recorde_name"); // 指定选择 DISTINCT 的字段

// 执行查询
        List<DataRecord> distinctRecordeNames = dataRecordMapper.selectList(queryWrapper);

// 转换结果为 List<Map<String, Object>>
        List<Map<String, Object>> list = distinctRecordeNames.stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("recorde_name", record.getRecorde_name());
                    return map;
                })
                .collect(Collectors.toList());

// 创建 DataModel 对象并设置数据
        DataModel ud = new DataModel();
        ud.setData(list);
        return ud;
    }

    // 修改参数设置
    @RequestMapping("/editSystemParameter")
    public DataModel editSystemParameter(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String recordeId = request.getParameter("id");
        String recordeName = request.getParameter("recorde_name");
        String recordeValue = request.getParameter("recorde_value");
        String status = request.getParameter("recorde_status");

        LambdaUpdateWrapper<DataRecord> lqw = Wrappers.lambdaUpdate();
        lqw
                .eq(DataRecord::getId, recordeId)
                .set(DataRecord::getRecorde_name, recordeName)
                .set(DataRecord::getRecorde_value, recordeValue)
                .set(DataRecord::getRecorde_status, status);
        int result = dataRecordMapper.update(null, lqw);

        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }

    // 删除参数设置
    @RequestMapping("/deleteSystemParameter")
    public DataModel deleteSystemParameter(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String id = request.getParameter("idsStr");
        int result = dataRecordMapper.deleteById(id);
        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }

    // 添加参数设置
    @RequestMapping("/addSystemParameter")
    public DataModel addSystemParameter(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String recordeName = request.getParameter("recorde_name");
        String recordeValue = request.getParameter("recorde_value");
        String status = request.getParameter("recorde_status");

        DataRecord build = DataRecord.builder().recorde_name(recordeName).recorde_status(Integer.valueOf(status)).recorde_value(recordeValue).build();
        int result = dataRecordMapper.insert(build);

        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }


    // 拿到全部数据
    @RequestMapping("/getAllSystemInfo")
    public DataModel getAllSystemInfo(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        ud = getInfo(ud, request);
        return ud;
    }


    // 查询初始数据
    public DataModel getInfo(DataModel ud, HttpServletRequest request) {
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        // 查总有多少条数据J  select count(*) total from UserInformation;
        Long total = dataRecordMapper.selectCount(null);

        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        LambdaQueryWrapper<DataRecord> lqw = Wrappers.lambdaQuery();
        lqw.last("limit " + limit + " offset " + skip);
        List<DataRecord> limitMaps = dataRecordMapper.selectList(lqw);
//        List<UserInformation> select = db.select(UserInformation.class, "select * from UserInformation");

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

    /**
     * 我的自定义语句保存到数据字典
     * adminCommentController操作
     */
    @PostMapping("/add")
    public int add(@RequestBody DataRecord dataRecord){
        return dataRecordMapper.insert(dataRecord);
    }

    /**
     * 商家回复的评论模板
     */
    @GetMapping("/get")
    public List<Map<String, Object>> update(@RequestBody DataRecord dataRecord){
        String sql = "select * from datarecord where recorde_name='shop_reply_template' and recorde_status=1";
        LambdaQueryWrapper<DataRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DataRecord::getRecorde_name, "shop_reply_template")
                .eq(DataRecord::getRecorde_status, 1);
        return dataRecordMapper.selectMaps(lambdaQueryWrapper);
    }

}
