package com.lureb.monitor.services;

import com.lureb.monitor.coinbase.model.Subscription;

public interface SubscriptionService {
    Subscription subscribe(Subscription subscription);
    Subscription unsubscribe(String uuid);
}
