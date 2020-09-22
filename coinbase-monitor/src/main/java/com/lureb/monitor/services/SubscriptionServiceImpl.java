package com.lureb.monitor.services;

import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.monitor.converter.ModelConverter;
import com.lureb.monitor.exceptions.AlreadyReported;
import com.lureb.monitor.exceptions.BadRequest;
import com.lureb.monitor.exceptions.NotFoundException;
import com.lureb.monitor.model.SubscriptionData;
import com.lureb.monitor.repository.SubscriptionRepository;
import com.lureb.monitor.socket.SocketHandler;
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
        Optional<SubscriptionData> subscriptionSaved = subscriptionRepository.findById(sha256hex);

        if (subscriptionSaved.isPresent()) {
            throw new AlreadyReported("Subscription already exists");
        }

        try {
            SubscriptionData subscriptionData = modelConverter.convertValue(subscription, SubscriptionData.class);
            subscriptionData.setUuid(sha256hex);
            subscriptionRepository.save(subscriptionData);
            log.info("Saved subscription {}", sha256hex);
            socketHandler.subscribe(subscription);
            return modelConverter.convertValue(subscriptionData, Subscription.class);
        } catch (IllegalArgumentException exception) {
            throw new BadRequest("Request is not valid: " + exception.getMessage());
        }
    }

    @Override
    public Subscription unsubscribe(String sha256hex) {
        Optional<SubscriptionData> subscriptionData = subscriptionRepository.findById(sha256hex);
        if (subscriptionData.isEmpty()) {
            throw new NotFoundException("Requested subscription not found (" + sha256hex + ")");
        }
        subscriptionRepository.delete(subscriptionData.get());
        socketHandler.unsubscribe();
        return modelConverter.convertValue(subscriptionData.get(), Subscription.class);
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
