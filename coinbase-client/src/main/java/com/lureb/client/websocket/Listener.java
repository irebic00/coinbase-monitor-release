package com.lureb.client.websocket;

import com.lureb.client.coinbase.model.TickerChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Listener {

    @Autowired
    private SimpMessagingTemplate template;

    @StreamListener(target = "coinbase")
    public void processMessage(TickerChannel tickerChannel){
        log.info("Read price from kafka {}", tickerChannel.getPrice());
        template.convertAndSend("/topic/coinbase", tickerChannel);
    }
}
