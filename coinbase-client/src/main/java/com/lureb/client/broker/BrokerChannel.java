package com.lureb.client.broker;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface BrokerChannel {

    @Input("coinbase")
    SubscribableChannel inbound();
}
