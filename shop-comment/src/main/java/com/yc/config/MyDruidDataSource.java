package com.yc.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;


@Configuration
@Slf4j
@RefreshScope
public class MyDruidDataSource {
    private DruidDataSource dataSource;

    @Value(value = "${spring.datasource.url}")
    private String dbUrl;

    @Value(value = "${spring.datasource.username}")
    private String dbUsername;

    @Value(value = "${spring.datasource.password}")
    private String dbPassword;

    @Value(value = "${spring.datasource.driver-class-name}")
    private String dbDriverClassName;

    @Primary
    @Bean
    @RefreshScope
    public DataSource dataSource() {
        log.info("开始加载数据源...");
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        dataSource.setDriverClassName(dbDriverClassName);
        return dataSource;
    }
}
