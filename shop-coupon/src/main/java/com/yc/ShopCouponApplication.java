package com.yc;

import com.yc.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient

@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)

@MapperScan("com.yc.mapper")
@EnableScheduling//开启定时任务
@EnableAsync//开启异步
public class ShopCouponApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(ShopCouponApplication.class, args);
    }
}
