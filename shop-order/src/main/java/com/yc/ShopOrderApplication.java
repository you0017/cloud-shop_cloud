package com.yc;

import com.yc.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient

@EnableFeignClients(basePackages = "com.yc.api" ,defaultConfiguration = DefaultFeignConfig.class) //openfeign

@MapperScan("com.yc.mapper")
@EnableTransactionManagement//事务
@EnableAsync//开启异步
public class ShopOrderApplication {
    public static void main( String[] args )
    {
        SpringApplication.run(ShopOrderApplication.class, args);
    }
}
