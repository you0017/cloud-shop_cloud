package com.yc.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@Log4j2
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        log.info("websocket初始化");
        return new ServerEndpointExporter();
    }
}
