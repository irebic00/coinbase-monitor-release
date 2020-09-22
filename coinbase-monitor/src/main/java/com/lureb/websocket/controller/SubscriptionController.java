package com.lureb.websocket.controller;

import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.websocket.services.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@Slf4j
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/subscription")
    public ResponseEntity<Subscription> subscription(
            @RequestBody Subscription subscriptionRequest) throws URISyntaxException {
        log.info("Received {}", subscriptionRequest);
        Subscription subscriptionResponse = subscriptionService.subscribe(subscriptionRequest);
        return ResponseEntity.created(new URI("/subscription/" + subscriptionResponse.getUuid()))
                .body(subscriptionResponse);
    }

    @DeleteMapping(value = "/subscription/{uuid}")
    public ResponseEntity<Void> unsubscribe(@PathVariable String uuid) {
        subscriptionService.unsubscribe(uuid);
        return ResponseEntity.noContent().build();
    }
}
