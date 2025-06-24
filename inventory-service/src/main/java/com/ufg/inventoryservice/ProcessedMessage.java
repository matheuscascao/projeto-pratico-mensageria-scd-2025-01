package com.ufg.inventoryservice;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "processed_orders")
public class ProcessedMessage {
    
    @Id
    private String orderId;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private Instant processedAt;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    public ProcessedMessage() {}
    
    public ProcessedMessage(String orderId, String status, String details) {
        this.orderId = orderId;
        this.status = status;
        this.details = details;
        this.processedAt = Instant.now();
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Instant getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
} 