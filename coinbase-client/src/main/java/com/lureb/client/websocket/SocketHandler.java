package com.lureb.client.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lureb.client.coinbase.model.TickerChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.io.IOException;

@Service
@Slf4j
public class SocketHandler implements WebSocketHandler {

    private final UnicastProcessor<TickerChannel> eventPublisher;
    private final Flux<TickerChannel> reactiveKafkaReceiver;
    private final Flux<String> outputEvents;
    private final ObjectMapper mapper;

    public SocketHandler(Flux<TickerChannel> reactiveKafkaReceiver) {
        this.reactiveKafkaReceiver = reactiveKafkaReceiver;

        mapper = new ObjectMapper();
        eventPublisher = UnicastProcessor.create();
        outputEvents = Flux.from(reactiveKafkaReceiver).map(this::toJSON);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session
                .receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::toTickerChannel)
                .doOnNext(eventPublisher::onNext)
                .zipWith(session.send(outputEvents.map(session::textMessage)))
                .then();
    }

    private TickerChannel toTickerChannel(String json) {
        try {
            return mapper.readValue(json, TickerChannel.class);
        } catch (IOException e) {
            log.warn("Did not receive proper JSON but got: {}", json);
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
}