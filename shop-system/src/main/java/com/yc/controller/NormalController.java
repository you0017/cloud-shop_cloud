package com.yc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.bean.DataRecord;
import com.yc.mapper.DataRecordMapper;
import com.yc.model.JsonModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@WebServlet("/html/normal.action")
@RestController
@RequestMapping("/normal")
@RequiredArgsConstructor
public class NormalController {
    private final DataRecordMapper dataRecordMapper;

    /**
     * 用于websocket动态获取网址
     */
    @GetMapping("/getServerInfo")
    public JsonModel getServerInfo(HttpServletRequest request) {
        Map<String, String> serverInfo = new HashMap<>();
        String protocol = request.isSecure() ? "wss" : "ws";
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();

        serverInfo.put("protocol", protocol);
        serverInfo.put("host", host);
        serverInfo.put("port", String.valueOf(port));
        serverInfo.put("contextPath", contextPath);

        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(serverInfo);
        return jm;
    }

    /**
     * 数据字典之类的
     */
    @GetMapping("/normal")
    public JsonModel getNormal() {
        LambdaQueryWrapper<DataRecord> db = new LambdaQueryWrapper<>();
        db.eq(DataRecord::getRecorde_status, 1);
        List<DataRecord> select = dataRecordMapper.selectList(db);
        return JsonModel.ok().setDate(select);
    }
}
