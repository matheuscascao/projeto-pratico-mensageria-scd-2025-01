package com.ufg.inventoryservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
    // Spring Data JPA gera automaticamente:
    // boolean existsById(String orderId) - para verificar idempotência
} 