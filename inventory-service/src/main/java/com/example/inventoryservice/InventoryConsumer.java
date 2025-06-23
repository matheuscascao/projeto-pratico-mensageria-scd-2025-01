
package com.example.inventoryservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "orders", groupId = "inventory-group")
    public void consume(String message) {
        System.out.println("Inventory recebeu: " + message);

        String status = Math.random() > 0.5 ? "success" : "failed";
        String inventoryEvent = String.format("{\"orderId\": \"%s\", \"status\": \"%s\"}", extractOrderId(message),
                status);

        kafkaTemplate.send("inventory-events", inventoryEvent);
        System.out.println("Inventory publicou: " + inventoryEvent);
    }

    private String extractOrderId(String message) {
        int start = message.indexOf("\"orderId\": \"") + 11;
        int end = message.indexOf("\",", start);
        return message.substring(start, end != -1 ? end : message.length() - 2);
    }
}
