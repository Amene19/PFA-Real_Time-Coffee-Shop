package com.coffeeshop.service;

import com.coffeeshop.model.DiningTable;
import java.util.List;

public interface TableService {
    List<DiningTable> getAllTables();
    List<DiningTable> getTablesByServer(String username);
    DiningTable getTableById(Long tableId);
    DiningTable updateTableStatus(Long tableId, String status);
}