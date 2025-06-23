package com.ufg.orderservice;

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
    
    @Autowired
    private OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        String orderId = UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();
        
        // Cria entidade Order
        Order order = new Order(orderId, timestamp);
        
        // Adiciona itens ao pedido
        for (ItemRequest itemRequest : orderRequest.getItems()) {
            Item item = new Item(itemRequest.getName(), itemRequest.getSku(), itemRequest.getQuantity());
            order.addItem(item);
        }
        
        // Salva no banco de dados
        orderRepository.save(order);
        
        String message = String.format("{\"orderId\": \"%s\", \"timestamp\": \"%s\", \"items\": %s}", 
                orderId, timestamp, orderRequest.getItems());
        kafkaTemplate.send("orders", message);
        
        return ResponseEntity.ok("Order enviado com sucesso! ID: " + orderId);
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
}

class ItemRequest {
    private String name;
    private String sku;
    private Integer quantity;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
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
}
        