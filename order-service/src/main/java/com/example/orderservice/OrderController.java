package com.ufg.orderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        String timestamp = Instant.now().toString();
        String messageContent = orderRequest.toString();
        String orderId = generateOrderId(timestamp, messageContent);
        
        String message = String.format("{\"orderId\": \"%s\", \"timestamp\": \"%s\", \"items\": %s}", 
                orderId, timestamp, orderRequest.getItems());
        
        // Salva no banco de dados
        Order order = new Order(orderId, timestamp, message);
        orderRepository.save(order);
        
        kafkaTemplate.send("orders", message);
        
        return ResponseEntity.ok("Order enviado com sucesso! ID: " + orderId);
    }
    
    private String generateOrderId(String timestamp, String message) {
        try {
            String input = timestamp + message;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return timestamp.replaceAll("[^0-9]", "").substring(0, 8) + "-" + hexString.toString().substring(0, 8);
        } catch (Exception e) {
            return timestamp.replaceAll("[^0-9]", "").substring(0, 8) + "-fallback";
        }
    }
}

class OrderRequest {
    private List<ItemRequest> items;
    
    public List<ItemRequest> getItems() { 
        return items; 
    }
    
    public void setItems(List<ItemRequest> items) { 
        this.items = items; 
    }
    
    @Override
    public String toString() {
        return items.toString();
    }
}

class ItemRequest {
    private String sku;
    private Integer quantity;
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return "{\"sku\":\"" + sku + "\",\"quantity\":" + quantity + "}";
    }
}
        