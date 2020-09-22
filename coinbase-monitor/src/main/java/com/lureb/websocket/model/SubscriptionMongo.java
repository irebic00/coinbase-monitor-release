package com.lureb.websocket.model;

import com.lureb.monitor.coinbase.model.ChannelType;
import com.lureb.monitor.coinbase.model.ProductIds;
import com.lureb.monitor.coinbase.model.SubscriptionType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document
public class SubscriptionMongo {
    @Id
    private String uuid;
    private SubscriptionType type;
    private List<ProductIds> productIds = new ArrayList<>();
    private List<ChannelType> channels = new ArrayList<>();

}