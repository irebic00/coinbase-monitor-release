package com.lureb.monitor.websocket;

import com.lureb.websocket.coinbase.model.Subscription;
import com.lureb.websocket.coinbase.model.TickerChannel;

public interface WebSocketMessagePublisher {

  public abstract void onNext(TickerChannel event);

  public abstract void onError(Throwable error);

  public abstract Subscription subscribe(Subscription subscription);

  public abstract boolean unsubscribe();

  public abstract boolean isSubscribed();
}
