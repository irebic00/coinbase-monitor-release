package com.lureb.client.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lureb.client.coinbase.model.TickerChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "kafka")
@Getter
@Setter
public class ReactiveKafkaConsumerConfig {

    private String bootstrapAddress;
    private String groupId;
    private String inTopicName;

    private final ObjectMapper objectMapper;

    public ReactiveKafkaConsumerConfig() {
        this.objectMapper = new ObjectMapper();
    }

    @Bean
    Map<String, Object> kafkaConsumerConfiguration() {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configuration.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configuration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return configuration;
    }

    @Bean
    ReceiverOptions<String, TickerChannel> kafkaReceiverOptions() {
        ReceiverOptions<String, TickerChannel> options = ReceiverOptions.create(kafkaConsumerConfiguration());
        return options.subscription(Collections.singletonList(inTopicName))
                .withKeyDeserializer(new StringDeserializer())
                .withValueDeserializer(new Deserializer<TickerChannel>() {
                    @SneakyThrows
                    @Override
                    public TickerChannel deserialize(String s, byte[] bytes) {
                        return objectMapper.readValue(s, TickerChannel.class);
                    }
                });
    }

    @Bean
    Flux<ReceiverRecord<String, TickerChannel>> reactiveKafkaReceiver(ReceiverOptions<String, TickerChannel> kafkaReceiverOptions) {
        return KafkaReceiver.create(kafkaReceiverOptions).receive();
    }
}
