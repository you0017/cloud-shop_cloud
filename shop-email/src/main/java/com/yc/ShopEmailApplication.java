package com.yc;

import com.yc.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.yc.api" ,defaultConfiguration = DefaultFeignConfig.class) //openfeign
public class ShopEmailApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(ShopEmailApplication.class, args);
    }
}
