package com.yc;

import com.yc.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.yc.mapper")

@EnableScheduling//开启定时任务
@EnableFeignClients(basePackages = "com.yc.api" ,defaultConfiguration = DefaultFeignConfig.class) //openfeign
public class ShopChatApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(ShopChatApplication.class, args);
    }
}
