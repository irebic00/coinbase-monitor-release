package com.lureb.monitor.repository;

import com.lureb.monitor.model.SubscriptionData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubscriptionRepository extends MongoRepository<SubscriptionData, String> {
}
