package com.lureb.monitor.socket.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "websocket.configuration")
public class WsSourceUri {
    private URI sourceUri;
}
