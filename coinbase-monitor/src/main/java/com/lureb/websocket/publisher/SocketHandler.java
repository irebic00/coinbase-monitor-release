package com.lureb.websocket.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.monitor.coinbase.model.TickerChannel;
import com.lureb.websocket.driver.WebSocketDriver;
import com.lureb.websocket.publisher.configurations.SocketConfiguration;
import com.lureb.websocket.publisher.configurations.SubscriptionResponse;
import com.lureb.websocket.publisher.configurations.WsSourceUri;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
public class SocketHandler implements WebSocketHandler {

  private final WsSourceUri wsSourceUri;
  private final KafkaTemplate<String, TickerChannel> kafkaTemplate;
  private final ObjectMapper mapper;
  private final WebSocketDriver webSocketDriver;

  private final Logger LOGGER = LogManager.getLogger(WebSocketHandler.class);

  public SocketHandler(WsSourceUri wsSourceUri, KafkaTemplate<String, TickerChannel> kafkaTemplate) {
    this.wsSourceUri = wsSourceUri;
    this.kafkaTemplate = kafkaTemplate;
    this.mapper = new ObjectMapper();
    webSocketDriver = new WebSocketDriver(wsSourceUri.getSourceUri());
  }

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    return session
        .receive()
        .map(WebSocketMessage::getPayloadAsText)
        .map(this::toTickerChannel)
        .doOnNext(this::send)
        .then();
  }

  private void send(TickerChannel event) {
    kafkaTemplate.send("coinbase", event);
  }

  private TickerChannel toTickerChannel(String json) {
    try {
      return mapper.readValue(json, TickerChannel.class);
    } catch (IOException e) {
      LOGGER.warn("Did not receive proper JSON but got: {}", json);
      throw new RuntimeException("Invalid JSON:" + json, e);
    }
  }

  public SubscriptionResponse subscribe(Subscription subscription) {
    LOGGER.info("Subscribing publisher source to {}", wsSourceUri.getSourceUri());
    SubscriptionResponse subscriptionResponse =
            SocketConfiguration.getInstance().createSubscription(subscription);
    if (!subscriptionResponse.getStatus().equals(SubscriptionResponse.Status.CREATED)) {
      return subscriptionResponse;
    }

    webSocketDriver.connectToWebSocket(subscription, kafkaTemplate, "coinbase");

    return subscriptionResponse;
  }

  public boolean unsubscribe() {
    webSocketDriver.disconnectFromWebSocket();
    return webSocketDriver.isConnected();
  }

  public boolean unsubscribe(String uuid) {
    SocketConfiguration.getInstance().deleteSubscription(uuid);
    return !webSocketDriver.isConnected();
  }
}
