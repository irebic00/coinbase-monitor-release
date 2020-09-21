package com.lureb.websocket.publisher;

import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.monitor.coinbase.model.TickerChannel;

public interface WebSocketMessagePublisher {

  public abstract void onNext(TickerChannel event);

  public abstract void onError(Throwable error);

  public abstract Subscription subscribe(Subscription subscription);

  public abstract boolean unsubscribe();

  public abstract boolean isSubscribed();
}
