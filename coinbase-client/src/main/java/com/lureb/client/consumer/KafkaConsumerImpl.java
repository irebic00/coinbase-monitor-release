package com.lureb.client.consumer;

import com.lureb.client.coinbase.model.TickerChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

@Service
@Slf4j
public class KafkaConsumerImpl implements KafkaConsumer {

    private final Flux<ReceiverRecord<String, TickerChannel>> reactiveKafkaReceiver;

    public KafkaConsumerImpl(Flux<ReceiverRecord<String, TickerChannel>> reactiveKafkaReceiver) {
        this.reactiveKafkaReceiver = reactiveKafkaReceiver;
    }

    @Override
    public Mono<TickerChannel> consume() {
        return reactiveKafkaReceiver.next().map(ConsumerRecord::value);
    }
}
