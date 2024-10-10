package com.yc.config;


import com.yc.interceptor.JwtTokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置类，注册web层相关组件
 */


@Configuration
@Slf4j
//适用于需要对Spring MVC进行部分自定义配置的场景，官方推荐使用这种方式，因为它简单且不会覆盖Spring MVC的默认配置。
//WebMvcConfigurationSupport类会覆盖默认配置
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    //springboot3不需要了
    /**
     * 设置静态资源映射  主要访问接口文档(html,js,css)
     * @param registry
     */
    /*protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**.action").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }*/


    /**
     * 设置静态资源映射  主要访问接口文档(html,js,css)
     * @param registry
     */
    /*@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加静态资源映射规则
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        //配置 knife4j 的静态资源请求映射地址
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }*/


    /**
     * 拦截器注册
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/item/**")
                .excludePathPatterns("/item/getHot","/item/association"
                        ,"/item/getCategories","/item/getBrandByCategories"
                        ,"/item/getBrands","/item/selectByPage"
                ,"/item/getItemById","/item/admin/**"
                ,"/inventory/admin","/finance/admin");
    }

    @Bean
    @LoadBalanced  // 这个注解使 RestTemplate 支持负载均衡
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
