package com.ufg.notificationservice;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @KafkaListener(topics = "inventory-events", groupId = "notification-group")
    public void consume(String message) {
        System.out.println("Notificação: Pedido processado - " + message);
    }
}
