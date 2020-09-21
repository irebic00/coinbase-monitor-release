package com.lureb.websocket.services;

import com.lureb.monitor.coinbase.model.Subscription;
import com.lureb.websocket.exceptions.AlreadyReported;
import com.lureb.websocket.exceptions.BadRequest;
import com.lureb.websocket.exceptions.NotFoundException;
import com.lureb.websocket.publisher.SocketHandler;
import com.lureb.websocket.publisher.configurations.SubscriptionResponse;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SocketHandler socketHandler;

    public SubscriptionServiceImpl(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    @Override
    public SubscriptionResponse subscribe(Subscription subscription) {

        // Check request and return bad request if not valid
        if (subscription.getChannels() == null || subscription.getProductIds() == null || subscription.getType() == null) {
            throw new BadRequest("Subscription cannot be created for " + subscription);
        }

        SubscriptionResponse createdSubscription = socketHandler.subscribe(subscription);

        if (createdSubscription.getStatus().equals(SubscriptionResponse.Status.ALREADY_REPORTED)) {
            throw new AlreadyReported("Subscription is already reported with uuid " + createdSubscription.getUuid());
        }

        return createdSubscription;
    }

    @Override
    public SubscriptionResponse unsubscribe(String uuid) {
        if (socketHandler.unsubscribe(uuid)) {
            return new SubscriptionResponse(uuid, SubscriptionResponse.Status.DELETED);
        } else {
            throw new NotFoundException("Requested subscription not found (" + uuid + ")");
        }
    }
}
