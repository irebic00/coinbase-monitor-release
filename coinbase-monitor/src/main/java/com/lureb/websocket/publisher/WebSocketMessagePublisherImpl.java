package com.lureb.websocket.publisher;

import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.monitor.coinbase.model.TickerChannel;
import com.lureb.websocket.driver.WebSocketDriver;
import com.lureb.websocket.publisher.configurations.WsSourceUri;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WebSocketMessagePublisherImpl implements WebSocketMessagePublisher {

  private final WsSourceUri wsSourceUri;

  private Subscription subscription;
  private final Logger LOGGER = LogManager.getLogger(WebSocketMessagePublisherImpl.class);
  private WebSocketDriver webSocketDriver;
  private final KafkaTemplate<String, TickerChannel> kafkaTemplate;

  public WebSocketMessagePublisherImpl(WsSourceUri wsSourceUri, KafkaTemplate<String, TickerChannel> kafkaTemplate) {
    this.wsSourceUri = wsSourceUri;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void onNext(TickerChannel event) {
    Optional<TickerChannel> lastReceivedEvent = Optional.of(event);
    kafkaTemplate.send("coinbase", event);
  }

  @Override
  public void onError(Throwable error) {
    LOGGER.error(error);
    error.printStackTrace();
  }

  @Override
  public Subscription subscribe(Subscription subscription) {
    webSocketDriver = new WebSocketDriver(wsSourceUri.getSourceUri());
    LOGGER.info("Subscribing publisher source to {}", wsSourceUri.getSourceUri());
    return webSocketDriver.connectToWebSocket(subscription, kafkaTemplate, "coinbase");
  }

  @Override
  public boolean unsubscribe() {
    webSocketDriver.disconnectFromWebSocket();
    return isSubscribed();
  }

  @Override
  public boolean isSubscribed() {
    return webSocketDriver.isConnected();
  }

}
