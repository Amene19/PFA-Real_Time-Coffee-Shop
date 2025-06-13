package com.coffeeshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coffeeshop.model.DiningTable;
import com.coffeeshop.model.TableStatus;

@Repository
public interface TableRepository extends JpaRepository<DiningTable, Long> {
    List<DiningTable> findByStatus(TableStatus status);
    Optional<DiningTable> findByNumber(Integer tableNumber);
    List<DiningTable> findByCapacityGreaterThanEqual(int capacity);
    long countByStatus(TableStatus status);
} 