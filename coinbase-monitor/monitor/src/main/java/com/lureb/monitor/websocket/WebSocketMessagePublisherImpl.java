package com.lureb.monitor.websocket;

import com.lureb.websocket.coinbase.model.Subscription;
import com.lureb.websocket.coinbase.model.TickerChannel;
import com.lureb.websocket.driver.Endpoint;
import com.lureb.websocket.driver.WebSocketDriver;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.UnicastProcessor;

@Service
public class WebSocketMessagePublisherImpl implements WebSocketMessagePublisher {
  private UnicastProcessor<TickerChannel> eventPublisher;
  private Subscription subscription;
  private Optional<TickerChannel> lastReceivedEvent = Optional.empty();
  private final Logger LOGGER = LogManager.getLogger(WebSocketMessagePublisherImpl.class);
  private WebSocketDriver<TickerChannel> webSocketDriver;

  public WebSocketMessagePublisherImpl(UnicastProcessor<TickerChannel> eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void onNext(TickerChannel event) {
    lastReceivedEvent = Optional.of(event);
    eventPublisher.onNext(event);
  }

  @Override
  public void onError(Throwable error) {
    LOGGER.error(error);
    error.printStackTrace();
  }

  @Override
  public Subscription subscribe(Subscription subscription) {
    webSocketDriver = new WebSocketDriver<>(Endpoint.BITCOIN_PRO);
    LOGGER.info("Subscribing publisher source to {}", Endpoint.BITCOIN_PRO);
    return webSocketDriver.connectToWebSocket(subscription, eventPublisher);
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
