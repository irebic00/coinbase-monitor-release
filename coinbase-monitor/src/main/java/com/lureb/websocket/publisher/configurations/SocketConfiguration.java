package com.lureb.websocket.publisher.configurations;

import com.lureb.monitor.coinbase.model.Subscription;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Holds entire configuration for sockets. This includes all parties: client to this application as
 * well as this application to coinbase. This application will open {@link
 * org.java_websocket.client.WebSocketClient} towards coinbase with {@link Subscription}.
 *
 *
 * @author ivan
 */
public class SocketConfiguration {

  private static SubscriptionConfiguration subscriptionConfiguration;
  private static volatile SocketConfiguration instance;
  private static final Object mutex = new Object();

  /** Default constructor */
  private SocketConfiguration() {}

  /**
   * Gets this instance
   *
   * @return this instance ({@link SubscriptionConfiguration})
   */
  public static SocketConfiguration getInstance() {
    SocketConfiguration result = instance;
    if (result == null) {
      synchronized (mutex) {
        result = instance;
        if (result == null) {
          instance = result = new SocketConfiguration();
          subscriptionConfiguration = new SubscriptionConfiguration();
        }
      }
    }
    return result;
  }

  /**
   * Adds new subscription but does not create new {@link
   * org.java_websocket.client.WebSocketClient}. Each subscription is unique as SHA-256 digest is
   * calculated from DAO.
   *
   * @param subscription subscription to create
   * @return SHA-256 digest
   */
  public SubscriptionResponse createSubscription(Subscription subscription) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      return new SubscriptionResponse(SubscriptionResponse.Status.ERROR);
    }
    if (messageDigest != null) {
      messageDigest.update(subscription.toString().getBytes());
    } else {
      return new SubscriptionResponse(SubscriptionResponse.Status.ERROR);
    }
    String uuid = DigestUtils.sha256Hex(subscription.toString());
    if (subscriptionConfiguration.getSubscriptions().contains(subscription)) {
      return new SubscriptionResponse(uuid, SubscriptionResponse.Status.ALREADY_REPORTED);
    }
    subscriptionConfiguration.getSubscriptions().put(uuid, subscription);
    return new SubscriptionResponse(uuid, SubscriptionResponse.Status.CREATED);
  }

  /**
   * Deletes subscription (but does not close socket). Subscription is identified by unique SHA-256
   * digest of subscription DAO
   *
   * @param uuid SHA-256 digest
   */
  public void deleteSubscription(String uuid) {
    subscriptionConfiguration.getSubscriptions().remove(uuid);
  }

  /**
   * Gets all subscriptions.
   *
   * @return all subscriptions
   */
  public ConcurrentHashMap<String, Subscription> getSubscriptions() {
    return subscriptionConfiguration.getSubscriptions();
  }
}
