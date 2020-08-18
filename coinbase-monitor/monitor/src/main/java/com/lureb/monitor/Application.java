package com.lureb.monitor;

import com.lureb.monitor.websocket.SocketHandler;
import com.lureb.websocket.coinbase.model.TickerChannel;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@ComponentScan("com.lureb")
public class Application {

  @Autowired private SocketHandler socketHandler;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public UnicastProcessor<TickerChannel> eventPublisher() {
    return UnicastProcessor.create();
  }

  @Bean
  public Flux<TickerChannel> events(UnicastProcessor<TickerChannel> eventPublisher) {
    return eventPublisher.replay(10).autoConnect();
  }

  @Bean
  public HandlerMapping webSocketMapping() {
    Map<String, Object> map = new HashMap<>();
    map.put("/websocket/coinbase", socketHandler.getSocketHandler());
    SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
    simpleUrlHandlerMapping.setUrlMap(map);

    // Without the order things break, why? :-/
    simpleUrlHandlerMapping.setOrder(10);
    return simpleUrlHandlerMapping;
  }

  @Bean
  public WebSocketHandlerAdapter handlerAdapter() {
    return new WebSocketHandlerAdapter();
  }
}
