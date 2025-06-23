
package com.example.orderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        String orderId = UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();
        String message = String.format("{\"orderId\": \"%s\", \"timestamp\": \"%s\", \"items\": %s}", orderId, timestamp, orderRequest.getItems());
        kafkaTemplate.send("orders", message);
        return ResponseEntity.ok("Order enviado com sucesso! ID: " + orderId);
    }
}

class OrderRequest {
    private List<String> items;
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
}
        