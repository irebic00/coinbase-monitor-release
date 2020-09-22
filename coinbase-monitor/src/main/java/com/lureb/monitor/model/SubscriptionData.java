package com.lureb.monitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lureb.monitor.coinbase.model.ChannelType;
import com.lureb.monitor.coinbase.model.ProductIds;
import com.lureb.monitor.coinbase.model.SubscriptionType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import static com.lureb.monitor.coinbase.model.Subscription.JSON_PROPERTY_PRODUCT_IDS;

@Data
@Document
public class SubscriptionData {
    @Id
    private String uuid;
    private SubscriptionType type;
    @JsonProperty(JSON_PROPERTY_PRODUCT_IDS)
    private List<ProductIds> productIds = new ArrayList<>();
    private List<ChannelType> channels = new ArrayList<>();

}