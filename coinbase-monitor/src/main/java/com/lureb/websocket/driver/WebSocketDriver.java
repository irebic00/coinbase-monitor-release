package com.lureb.websocket.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.monitor.coinbase.model.TickerChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.kafka.core.KafkaTemplate;

import java.net.URI;
import java.util.Objects;

public class WebSocketDriver {
  private final URI uri;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private WebSocketClient webSocketClient = null;
  private final Logger LOGGER = LogManager.getLogger(WebSocketDriver.class);

  public WebSocketDriver(URI uri) {
    this.uri = uri;
  }

  public Subscription connectToWebSocket(
          Subscription subscription, KafkaTemplate<String, TickerChannel> kafkaTemplate, String kafkaTopic) {

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
              kafkaTemplate.send(kafkaTopic, tc);
            }
          }

          @Override
          public void onClose(int i, String s, boolean b) {
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

  public void disconnectFromWebSocket() {
    if (webSocketClient != null) {
      webSocketClient.close();
    }
  }

  public boolean isConnected() {
    return webSocketClient.isOpen();
  }
}
