package com.ufg.inventoryservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class InventoryConsumer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private ProcessedMessageRepository processedMessageRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "orders", groupId = "inventory-group")
    @Transactional
    public void consume(String message) {
        System.out.println("Inventory recebeu: " + message);
        
        try {
            JsonNode orderJson = objectMapper.readTree(message);
            String orderId = orderJson.get("orderId").asText();
            JsonNode itemsArray = orderJson.get("items");
            
            if (processedMessageRepository.existsById(orderId)) {
                System.out.println("Pedido já processado (idempotência): " + orderId);
                return;
            }
            
            for (JsonNode itemNode : itemsArray) {
                String sku = itemNode.get("sku").asText();
                int quantity = itemNode.get("quantity").asInt();
                
                Optional<Item> itemOpt = itemRepository.findBySku(sku);
                if (!itemOpt.isPresent()) {
                    throw new RuntimeException("Item não encontrado: " + sku);
                }
                if (itemOpt.get().getQuantity() < quantity) {
                    throw new RuntimeException("Quantidade insuficiente para " + sku + 
                        " (disponível: " + itemOpt.get().getQuantity() + 
                        ", solicitado: " + quantity + ")");
                }
            }
            
            for (JsonNode itemNode : itemsArray) {
                String sku = itemNode.get("sku").asText();
                int quantity = itemNode.get("quantity").asInt();
                
                Item item = itemRepository.findBySku(sku).get();
                item.setQuantity(item.getQuantity() - quantity);
                itemRepository.save(item);
            }
            
            ProcessedMessage processedMessage = new ProcessedMessage(orderId, "success", 
                "Inventário reduzido com sucesso");
            processedMessageRepository.save(processedMessage);
            
            System.out.println("Inventário reduzido para pedido: " + orderId);
            
            // 📤 Envia evento de sucesso
            String inventoryEvent = String.format(
                "{\"orderId\": \"%s\", \"status\": \"success\", \"details\": \"Inventário reduzido com sucesso\"}", 
                orderId
            );
            kafkaTemplate.send("inventory-events", inventoryEvent);
            System.out.println("Inventory publicou: " + inventoryEvent);
            
        } catch (Exception e) {
            System.err.println("Erro ao processar pedido: " + e.getMessage());
            String orderId = extractOrderId(message);
            String inventoryEvent = String.format(
                "{\"orderId\": \"%s\", \"status\": \"failed\", \"details\": \"%s\"}", 
                orderId, e.getMessage()
            );
            kafkaTemplate.send("inventory-events", inventoryEvent);
            
            throw new RuntimeException("Falha no processamento do pedido: " + orderId, e);
        }
    }

    private String extractOrderId(String message) {
        try {
            int start = message.indexOf("\"orderId\": \"") + 12;
            int end = message.indexOf("\",", start);
            if (end == -1) end = message.indexOf("\"}", start);
            return message.substring(start, end);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
