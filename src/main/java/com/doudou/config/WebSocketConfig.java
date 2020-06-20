package com.doudou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author DouDou
 * @Description
 * @date 2020/6/20
 */
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter getServerEndpointExporter(){
        return new ServerEndpointExporter();
    }

}
