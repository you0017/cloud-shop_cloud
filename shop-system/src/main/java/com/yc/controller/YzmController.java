package com.yc.controller;


import com.aliyun.captcha20230305.models.VerifyCaptchaRequest;
import com.aliyun.captcha20230305.models.VerifyCaptchaResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.yc.model.JsonModel;
import com.yc.utils.AliOSSProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

//@WebServlet(value = "/html/yzm.action")
@RestController
@RequestMapping("/yzm")  // 使用 @RequestMapping 来定义基本路径
public class YzmController {

    @Autowired
    private AliOSSProperties aliOSSProperties;

    @PostMapping("/regYzm")
    public Map<String, Object> regYzm(@RequestParam("captchaVerifyParam") String captchaVerifyParam, HttpServletResponse resp) throws Exception {
        JsonModel jm = new JsonModel();
        Map<String, Object> map = new HashMap<>();

        // ====================== 1. 初始化配置 ======================
        Config config = new Config();
        config.accessKeyId = aliOSSProperties.getAccessKeyId();
        config.accessKeySecret = aliOSSProperties.getAccessKeySecret();
        config.endpoint = "captcha.cn-shanghai.aliyuncs.com";
        config.connectTimeout = 5000;
        config.readTimeout = 5000;

        // ====================== 2. 初始化客户端 ======================
        com.aliyun.captcha20230305.Client client = new com.aliyun.captcha20230305.Client(config);
        VerifyCaptchaRequest request = new VerifyCaptchaRequest();
        request.captchaVerifyParam = captchaVerifyParam;

        try {
            // ====================== 3. 发起请求并获取结果 ======================
            VerifyCaptchaResponse response = client.verifyCaptcha(request);
            Boolean captchaVerifyResult = response.body.result.verifyResult; // 验证结果

            map.put("code", captchaVerifyResult);
        } catch (TeaException error) {
            // 处理异常情况，优先保证业务可用
            map.put("code", true); // 默认认为验证通过
            jm.setError(error.getMessage()); // 记录错误信息
        } catch (Exception error) {
            // 处理未知异常情况
            map.put("code", true); // 默认认为验证通过
            jm.setError(error.getMessage()); // 记录错误信息
        }
        return map;
    }
}
