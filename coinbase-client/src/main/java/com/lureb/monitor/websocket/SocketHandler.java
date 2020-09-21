package com.lureb.monitor.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lureb.monitor.controller.model.SubscriptionResponse;
import com.lureb.monitor.websocket.configurations.SocketConfiguration;
import com.lureb.websocket.coinbase.model.Subscription;
import com.lureb.websocket.coinbase.model.TickerChannel;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

@Component
public class SocketHandler implements WebSocketHandler {

  private final WebSocketMessagePublisher webSocketMessagePublisher;
  private final UnicastProcessor<TickerChannel> eventPublisher;
  private final Flux<String> outputEvents;
  private final ObjectMapper mapper;
  private final Logger LOGGER = LogManager.getLogger(WebSocketHandler.class);

  public SocketHandler(WebSocketMessagePublisher webSocketMessagePublisher, UnicastProcessor<TickerChannel> eventPublisher, Flux<TickerChannel> events) {
    this.webSocketMessagePublisher = webSocketMessagePublisher;
    this.eventPublisher = eventPublisher;
    this.outputEvents = Flux.from(events).map(this::toJSON);
    this.mapper = new ObjectMapper();
  }

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    return session
        .receive()
        .map(WebSocketMessage::getPayloadAsText)
        .map(this::toTickerChannel)
        .doOnNext(webSocketMessagePublisher::onNext)
        .zipWith(session.send(outputEvents.map(session::textMessage)))
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

  private String toJSON(TickerChannel event) {
    try {
      return mapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public SubscriptionResponse subscribe(Subscription subscriptionRequest) {
    SubscriptionResponse subscriptionResponse =
        SocketConfiguration.getInstance().createSubscription(subscriptionRequest);
    if (!subscriptionResponse.getStatus().equals(SubscriptionResponse.Status.CREATED)) {
      return subscriptionResponse;
    }
    this.webSocketMessagePublisher.subscribe(
        SocketConfiguration.getInstance().getSubscriptions().get(subscriptionResponse.getUuid()));
    return subscriptionResponse;
  }

  public boolean unsubscribe(String uuid) {
    this.webSocketMessagePublisher.unsubscribe();
    SocketConfiguration.getInstance().deleteSubscription(uuid);
    return !this.webSocketMessagePublisher.isSubscribed();
  }
}
