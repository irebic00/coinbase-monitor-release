package com.lureb.websocket.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.monitor.coinbase.model.TickerChannel;
import com.lureb.websocket.driver.WebSocketDriver;
import com.lureb.websocket.kafka.publisher.KafkaPublisher;
import com.lureb.websocket.socket.configurations.WsSourceUri;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
public class SocketHandler implements WebSocketHandler {

  private final WsSourceUri wsSourceUri;
  private final KafkaPublisher kafkaPublisher;
  private final ObjectMapper mapper;
  private final WebSocketDriver webSocketDriver;

  private final Logger LOGGER = LogManager.getLogger(WebSocketHandler.class);

  public SocketHandler(WsSourceUri wsSourceUri, KafkaPublisher kafkaPublisher) {
    this.wsSourceUri = wsSourceUri;
    this.kafkaPublisher = kafkaPublisher;
    this.mapper = new ObjectMapper();
    webSocketDriver = new WebSocketDriver(wsSourceUri.getSourceUri());
  }

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    return session
        .receive()
        .map(WebSocketMessage::getPayloadAsText)
        .map(this::toTickerChannel)
        .doOnNext(kafkaPublisher::send)
        .then();
  }

  private TickerChannel toTickerChannel(String json) {
    try {
      return mapper.readValue(json, TickerChannel.class);
    } catch (IOException e) {
      LOGGER.warn("Did not receive proper JSON but got: {}", json);
      throw new RuntimeException("Invalid JSON:" + json, e);
    }
  }

  public boolean subscribe(Subscription subscription) {
    webSocketDriver.connectToWebSocket(subscription, kafkaPublisher);
    return webSocketDriver.isConnected();
  }

  public boolean unsubscribe() {
    webSocketDriver.disconnectFromWebSocket();
    return !webSocketDriver.isConnected();
  }
}
