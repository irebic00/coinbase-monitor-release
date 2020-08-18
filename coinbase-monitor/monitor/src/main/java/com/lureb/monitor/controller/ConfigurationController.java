package com.lureb.monitor.controller;

import com.lureb.monitor.controller.model.SubscriptionResponse;
import com.lureb.monitor.websocket.SocketHandler;
import com.lureb.websocket.coinbase.model.Subscription;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ConfigurationController {

  @Autowired private SocketHandler socketHandler;

  @PostMapping("/subscription")
  public ResponseEntity<SubscriptionResponse> subscription(
      @RequestBody Subscription subscriptionRequest) {
    SubscriptionResponse createdSubscription = socketHandler.subscribe(subscriptionRequest);

    // Check request and return bad request if not valid
    if (subscriptionRequest.getChannels() == null
        || subscriptionRequest.getProductIds() == null
        || subscriptionRequest.getType() == null) {
      return ResponseEntity.badRequest().build();
    }

    if (createdSubscription.getStatus().equals(SubscriptionResponse.Status.ALREADY_EXISTS)) {
      return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(createdSubscription);
    } else if (createdSubscription.getStatus().equals(SubscriptionResponse.Status.ERROR)) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } else {
      try {
        return ResponseEntity.created(new URI("/subscription/" + createdSubscription.getUuid()))
            .body(createdSubscription);
      } catch (URISyntaxException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
    }
  }

  @RequestMapping(value = "/subscription/{uuid}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> unsubscribe(@PathVariable String uuid) {
    if (socketHandler.unsubscribe(uuid)) {
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
