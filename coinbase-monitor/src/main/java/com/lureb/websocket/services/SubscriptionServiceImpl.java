package com.lureb.websocket.services;

import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.websocket.converter.ModelConverter;
import com.lureb.websocket.exceptions.AlreadyReported;
import com.lureb.websocket.exceptions.BadRequest;
import com.lureb.websocket.exceptions.NotFoundException;
import com.lureb.websocket.model.SubscriptionMongo;
import com.lureb.websocket.repository.SubscriptionRepository;
import com.lureb.websocket.socket.SocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SocketHandler socketHandler;
    private final SubscriptionRepository subscriptionRepository;
    private final ModelConverter modelConverter;

    public SubscriptionServiceImpl(SocketHandler socketHandler, SubscriptionRepository subscriptionRepository, ModelConverter modelConverter) {
        this.socketHandler = socketHandler;
        this.subscriptionRepository = subscriptionRepository;
        this.modelConverter = modelConverter;
    }

    @Override
    public Subscription subscribe(Subscription subscription) {

        if (subscription.getChannels() == null || subscription.getProductIds() == null || subscription.getType() == null) {
            throw new BadRequest("Subscription cannot be created for " + subscription);
        }

        String sha256hex = calculateSHA256(subscription);
        if (sha256hex == null) {
            throw new BadRequest("Subscription cannot be created for " + subscription);
        }
        Optional<SubscriptionMongo> subscriptionSaved = subscriptionRepository.findById(sha256hex);

        if (subscriptionSaved.isPresent()) {
            throw new AlreadyReported("Subscription already exists");
        }

        try {
            SubscriptionMongo subscriptionMongo = modelConverter.convertValue(subscription, SubscriptionMongo.class);
            if (socketHandler.subscribe(subscription)) {
                subscriptionRepository.save(subscriptionMongo);
            }
            return modelConverter.convertValue(subscriptionMongo, Subscription.class);
        } catch (IllegalArgumentException exception) {
            throw new BadRequest("Request is not valid: " + exception.getMessage());
        }
    }

    @Override
    public Subscription unsubscribe(String sha256hex) {
        Optional<SubscriptionMongo> subscriptionMongo = subscriptionRepository.findById(sha256hex);
        if (subscriptionMongo.isEmpty()) {
            throw new NotFoundException("Requested subscription not found (" + sha256hex + ")");
        }
        socketHandler.unsubscribe();
        return modelConverter.convertValue(subscriptionMongo.get(), Subscription.class);
    }

    private String calculateSHA256(Subscription subscription) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to create subscription", e);
            return null;
        }
        messageDigest.update(subscription.toString().getBytes());

        return DigestUtils.sha256Hex(subscription.toString());
    }
}
