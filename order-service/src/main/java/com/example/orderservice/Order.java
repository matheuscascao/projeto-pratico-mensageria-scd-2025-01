package com.ufg.orderservice;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    private String orderId;
    
    @Column(nullable = false)
    private String timestamp;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();
    
    public Order() {}
    
    public Order(String orderId, String timestamp) {
        this.orderId = orderId;
        this.timestamp = timestamp;
    }
    
    // Método auxiliar para adicionar itens
    public void addItem(Item item) {
        items.add(item);
        item.setOrder(this);
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public List<Item> getItems() {
        return items;
    }
    
    public void setItems(List<Item> items) {
        this.items = items;
    }
} 