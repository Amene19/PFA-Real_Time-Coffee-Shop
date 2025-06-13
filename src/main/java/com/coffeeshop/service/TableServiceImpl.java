package com.coffeeshop.service;

import com.coffeeshop.model.DiningTable;
import com.coffeeshop.model.TableStatus;
import com.coffeeshop.repository.TableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class TableServiceImpl implements TableService {

    private final TableRepository tableRepository;

    public TableServiceImpl(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    @Override
    public List<DiningTable> getAllTables() {
        return tableRepository.findAll();
    }

    @Override
    public List<DiningTable> getTablesByServer(String username) {
        // For now, return all tables since we don't have a table assignment system
        return tableRepository.findAll();
    }

    @Override
    public DiningTable getTableById(Long tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));
    }

    @Override
    public DiningTable updateTableStatus(Long tableId, String status) {
        DiningTable table = getTableById(tableId);
        table.setStatus(TableStatus.valueOf(status.toUpperCase()));
        return tableRepository.save(table);
    }
} 
 