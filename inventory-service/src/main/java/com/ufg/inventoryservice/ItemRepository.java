package com.ufg.inventoryservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findBySku(String sku);
    
    // Reduz quantidade diretamente no banco, para evitar BOS de concorrência
    @Modifying
    @Query("UPDATE Item i SET i.quantity = i.quantity - :amount WHERE i.sku = :sku AND i.quantity >= :amount")
    int reduceQuantityAtomically(@Param("sku") String sku, @Param("amount") Integer amount);
    
    @Query("SELECT CASE WHEN i.quantity >= :amount THEN true ELSE false END FROM Item i WHERE i.sku = :sku")
    Boolean hasEnoughStock(@Param("sku") String sku, @Param("amount") Integer amount);
    
    @Query("SELECT i.quantity FROM Item i WHERE i.sku = :sku")
    Optional<Integer> getCurrentQuantity(@Param("sku") String sku);
} 