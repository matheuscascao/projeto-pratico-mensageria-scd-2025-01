package com.ufg.inventoryservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
    public void consume(String message) {
        System.out.println("Inventory recebeu: " + message);
        
        try {
            JsonNode orderJson = objectMapper.readTree(message);
            String orderId = orderJson.get("orderId").asText();
            JsonNode itemsArray = orderJson.get("items");
            
            if (processedMessageRepository.existsById(orderId)) {
                System.out.println("Pedido já processado (idempotência): " + orderId);
                return; // Pula o processamento
            }
            
            boolean allItemsAvailable = true;
            StringBuilder errorMessage = new StringBuilder();
            
            // Verifica se todos os itens estão disponíveis
            for (JsonNode itemNode : itemsArray) {
                String sku = itemNode.get("sku").asText();
                int quantity = itemNode.get("quantity").asInt();
                
                Optional<Item> itemOpt = itemRepository.findBySku(sku);
                if (!itemOpt.isPresent()) {
                    allItemsAvailable = false;
                    errorMessage.append("Item não encontrado: ").append(sku).append("; ");
                } else if (itemOpt.get().getQuantity() < quantity) {
                    allItemsAvailable = false;
                    errorMessage.append("Quantidade insuficiente para ").append(sku)
                              .append(" (disponível: ").append(itemOpt.get().getQuantity())
                              .append(", solicitado: ").append(quantity).append("); ");
                }
            }
            
            String status;
            String details = "";
            
            if (allItemsAvailable) {
                // Reduz as quantidades
                for (JsonNode itemNode : itemsArray) {
                    String sku = itemNode.get("sku").asText();
                    int quantity = itemNode.get("quantity").asInt();
                    
                    Item item = itemRepository.findBySku(sku).get();
                    item.setQuantity(item.getQuantity() - quantity);
                    itemRepository.save(item);
                }
                status = "success";
                details = "Inventário reduzido com sucesso";
                System.out.println("Inventário reduzido para pedido: " + orderId);
            } else {
                status = "failed";
                details = errorMessage.toString();
                System.out.println("Falha no inventário para pedido: " + orderId + " - " + details);
            }
            
            ProcessedMessage processedMessage = new ProcessedMessage(orderId, status, details);
            processedMessageRepository.save(processedMessage);
            
            String inventoryEvent = String.format(
                "{\"orderId\": \"%s\", \"status\": \"%s\", \"details\": \"%s\"}", 
                orderId, status, details
            );
            
            kafkaTemplate.send("inventory-events", inventoryEvent);
            System.out.println("Inventory publicou: " + inventoryEvent);
            
        } catch (Exception e) {
            System.err.println("Erro ao processar pedido: " + e.getMessage());
            String orderId = extractOrderId(message);
            
            try {
                ProcessedMessage processedMessage = new ProcessedMessage(orderId, "failed", 
                    "Erro no processamento: " + e.getMessage());
                processedMessageRepository.save(processedMessage);
            } catch (Exception saveError) {
                System.err.println("Erro ao salvar processamento: " + saveError.getMessage());
            }
            
            String inventoryEvent = String.format(
                "{\"orderId\": \"%s\", \"status\": \"failed\", \"details\": \"Erro no processamento: %s\"}", 
                orderId, e.getMessage()
            );
            kafkaTemplate.send("inventory-events", inventoryEvent);
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
