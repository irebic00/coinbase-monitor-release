package com.lureb.monitor.kafka.publisher;

import com.lureb.monitor.coinbase.model.TickerChannel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class KafkaPublisher {
    private final KafkaTemplate<String, TickerChannel> kafkaTemplate;

    public KafkaPublisher(KafkaTemplate<String, TickerChannel> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(TickerChannel tickerChannel) {
        kafkaTemplate.send("coinbase", tickerChannel);
    }
}
