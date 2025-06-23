package com.ufg.orderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existem itens no banco para evitar duplicação
        if (itemRepository.count() == 0) {
            initializeItems();
        }
    }

    private void initializeItems() {
        // Criando itens de exemplo para o inventário
        Item item1 = new Item("Smartphone Samsung", "SAMS-001", 50);
        Item item2 = new Item("Notebook Dell", "DELL-002", 25);
        Item item3 = new Item("Mouse Wireless", "MOUS-003", 100);
        Item item4 = new Item("Teclado Mecânico", "TECL-004", 75);
        Item item5 = new Item("Monitor 24\"", "MONI-005", 30);
        Item item6 = new Item("Headset Gamer", "HEAD-006", 60);
        Item item7 = new Item("Webcam HD", "WEBC-007", 40);
        Item item8 = new Item("Carregador USB-C", "CARR-008", 120);

        // Salvando no banco de dados
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);
        itemRepository.save(item5);
        itemRepository.save(item6);
        itemRepository.save(item7);
        itemRepository.save(item8);

        System.out.println("Itens de exemplo criados no banco de dados!");
    }
} 