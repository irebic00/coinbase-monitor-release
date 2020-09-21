package com.lureb.monitor.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Getter
@Setter
@ConfigurationProperties(prefix = "websocket.configuration")
public class WsSourceUri {
    private URI sourceUri;
}
