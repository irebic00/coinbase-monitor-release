package com.lureb.websocket.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lureb.websocket.coinbase.model.Subscription;
import com.lureb.websocket.coinbase.model.TickerChannel;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.reactivestreams.Subscriber;

public class WebSocketDriver<T> {
  private final String endpoint;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private WebSocketClient webSocketClient = null;
  private final Logger LOGGER = LogManager.getLogger(WebSocketDriver.class);

  public WebSocketDriver(Endpoint endpoint) {
    this.endpoint = endpoint.getValue();
  }

  public Subscription connectToWebSocket(
      Subscription subscription, Subscriber<? super T> subscriber) {
    URI uri;
    try {
      uri = new URI(this.endpoint);
    } catch (URISyntaxException e) {
      LOGGER.error(e.getMessage());
      return null;
    }

    webSocketClient =
        new WebSocketClient(uri) {
          @Override
          public void onOpen(ServerHandshake serverHandshake) {
            try {
              webSocketClient.send(objectMapper.writeValueAsString(subscription));
            } catch (JsonProcessingException e) {
              LOGGER.error("Failed to subscribe with " + subscription.toString() + " to " + uri);
            }
            LOGGER.info("Websocket opened for subscription " + subscription);
          }

          @Override
          public void onMessage(String s) {
            TickerChannel tc = null;
            Subscription sc;
            try {
              tc = objectMapper.readValue(s, TickerChannel.class);
            } catch (JsonProcessingException e) {
              try {
                sc = objectMapper.readValue(s, Subscription.class);
                LOGGER.info("Subscription answer: " + sc);
              } catch (JsonProcessingException ex) {
                LOGGER.trace(
                    "tc and sc not initialized. What was not mapped to TickerChannel or Subscription: "
                        + s);
                LOGGER.trace(ex.getMessage());
              }
              LOGGER.debug(e.getMessage());
            }
            LOGGER.info(
                "Got update at: {} {}",
                (tc == null ? "Subscription" : Objects.requireNonNull(tc.getPrice()).toString()),
                (tc == null ? "successful" : Objects.requireNonNull(tc).getProductId()));
            LOGGER.debug(subscription);
            if (tc != null) {
              subscriber.onNext((T) tc);
            }
          }

          @Override
          public void onClose(int i, String s, boolean b) {
            subscriber.onComplete();
            LOGGER.info(
                "Websocket closed for subscription {}, code: {}, reason: {}, remote: {}",
                subscription,
                i,
                s,
                b);
          }

          @Override
          public void onError(Exception e) {
            LOGGER.info("Websocket error " + e.getMessage());
          }
        };
    webSocketClient.connect();
    return subscription;
  }

  public boolean disconnectFromWebSocket() {
    if (webSocketClient != null) {
      webSocketClient.close();
      return webSocketClient.isClosed();
    }
    return true;
  }

  public boolean isConnected() {
    return webSocketClient.isOpen();
  }
}
