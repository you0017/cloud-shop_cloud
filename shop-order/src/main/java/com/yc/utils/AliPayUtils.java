package com.yc.utils;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
 
/**
 * @method: $
 * @description: 
 * @date: $
 * @author: myth
 * @return $
 */
public class AliPayUtils {
 
    public static AlipayClient alipayClient;
 
    static {
        alipayClient =
                new DefaultAlipayClient(
                        "https://openapi-sandbox.dl.alipaydev.com/gateway.do",
                        "123123123",
                        "MIIEvgIBADANBgkq.......",
                        "json",
                        //UTF-8编码格式
                        "UTF-8",
                        "MIIBIjANBgkqhk......",
                        //RSA非对称加密
                        "RSA2");
    }
 
    public static AlipayClient getAlipayClient() {
        return alipayClient;
    }
}