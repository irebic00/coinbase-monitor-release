package com.lureb.monitor.consumer;

import com.lureb.client.coinbase.model.TickerChannel;
import reactor.core.publisher.Mono;

public interface KafkaConsumer {
    Mono<TickerChannel> consume();
}
