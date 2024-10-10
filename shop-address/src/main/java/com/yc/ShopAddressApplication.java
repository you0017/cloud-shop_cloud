package com.yc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.yc.mapper")
@EnableTransactionManagement//事务
public class ShopAddressApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(ShopAddressApplication.class, args);
    }
}
