package com.yc.config;

import com.yc.context.BaseContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {

    //日志
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

    //添加请求头
    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String userId = BaseContext.getCurrentId();
                if (userId != null){
                    template.header("userId", userId);
                }
            }
        };
    }
}