package com.lureb.websocket.publisher.configurations;

public class SubscriptionResponse {
  private String uuid;
  private Status status;

  public SubscriptionResponse(String uuid, Status status) {
    this.uuid = uuid;
    this.status = status;
  }

  public SubscriptionResponse(Status status) {
    this.status = status;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public enum Status {
    ALREADY_REPORTED("ALREADY_REPORTED"),
    ERROR("ERROR"),
    CREATED("CREATED"),
    DELETED("DELETED");

    private final String status;

    Status(String status) {
      this.status = status;
    }

    public String getValue() {
      return status;
    }

    @Override
    public String toString() {
      return String.valueOf(status);
    }
  }
}
