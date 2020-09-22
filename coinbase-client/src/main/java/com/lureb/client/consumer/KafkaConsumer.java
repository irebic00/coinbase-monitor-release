package com.lureb.client.consumer;

import com.lureb.client.coinbase.model.TickerChannel;
import reactor.core.publisher.Mono;

public interface KafkaConsumer {
    Mono<TickerChannel> consume();
}
