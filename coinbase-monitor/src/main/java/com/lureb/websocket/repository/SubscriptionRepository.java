package com.lureb.websocket.repository;

import com.lureb.websocket.model.SubscriptionMongo;
import org.springframework.data.repository.CrudRepository;

public interface SubscriptionRepository extends CrudRepository<SubscriptionMongo, String> {
}
