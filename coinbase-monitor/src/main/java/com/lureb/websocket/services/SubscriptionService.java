package com.lureb.websocket.services;

import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.websocket.publisher.configurations.SubscriptionResponse;

public interface SubscriptionService {
    SubscriptionResponse subscribe(Subscription subscription);
    SubscriptionResponse unsubscribe(String uuid);
}
