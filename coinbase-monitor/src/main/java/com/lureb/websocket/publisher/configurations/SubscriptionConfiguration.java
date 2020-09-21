package com.lureb.websocket.publisher.configurations;

import com.lureb.monitor.coinbase.model.Subscription;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionConfiguration {

  private ConcurrentHashMap<String, Subscription> subscriptions;

  public SubscriptionConfiguration() {
    this.subscriptions = new ConcurrentHashMap<>();
  }

  public ConcurrentHashMap<String, Subscription> getSubscriptions() {
    return subscriptions;
  }

  public void setSubscriptions(ConcurrentHashMap<String, Subscription> subscriptions) {
    this.subscriptions = subscriptions;
  }
}
