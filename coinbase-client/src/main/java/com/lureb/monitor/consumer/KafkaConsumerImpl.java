package com.lureb.monitor.consumer;

import com.lureb.client.coinbase.model.TickerChannel;
import reactor.core.publisher.Mono;

public class KafkaConsumerImpl implements KafkaConsumer {
    @Override
    public Mono<TickerChannel> consume() {
        return null;
    }
}
