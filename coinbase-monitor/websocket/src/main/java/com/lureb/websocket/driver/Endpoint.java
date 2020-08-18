package com.lureb.websocket.driver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Gets or Sets Endpoint */
public enum Endpoint {
  BITCOIN_PRO("wss://ws-feed.pro.coinbase.com");

  private String value;

  Endpoint(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static Endpoint fromValue(String value) {
    for (Endpoint b : Endpoint.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
