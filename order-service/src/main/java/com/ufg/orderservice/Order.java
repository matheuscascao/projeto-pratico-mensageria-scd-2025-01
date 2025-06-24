package com.ufg.orderservice;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    private String orderId;
    
    @Column(nullable = false)
    private String timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    public Order() {}
    
    public Order(String orderId, String timestamp, String message) {
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.message = message;
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
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
} 