package com.lureb.client.controller;

import com.lureb.client.coinbase.model.TickerChannel;
import com.lureb.client.consumer.KafkaConsumer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class MainController {

  private final KafkaConsumer kafkaConsumer;

  public MainController(KafkaConsumer kafkaConsumer) {
    this.kafkaConsumer = kafkaConsumer;
  }

  @GetMapping("/")
  public String index(final Model model) {
    return "index";
  }

  @GetMapping(path = "/websocket/coinbase", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Mono<TickerChannel> ticker() {
    return kafkaConsumer.consume();
  }
}
