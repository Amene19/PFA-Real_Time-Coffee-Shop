package com.coffeeshop.dto;

import com.coffeeshop.model.DiningTable;
import com.coffeeshop.model.TableStatus;
import lombok.Getter;

@Getter
public class TableView {
    private final DiningTable table;
    private final String statusClass;
    private final String statusBadge;
    private final String statusText;
    private final boolean canCreateOrder;
    private final boolean canEditOrder;
    private final boolean canMarkAvailable;

    public TableView(DiningTable table) {
        this.table = table;
        TableStatus status = table.getStatus();
        
        this.statusClass = "table-status-" + status.name().toLowerCase();
        this.statusBadge = switch (status) {
            case AVAILABLE -> "bg-success";
            case OCCUPIED -> "bg-warning";
            case RESERVED -> "bg-info";
        };
        this.statusText = status.name().substring(0, 1).toUpperCase() + 
                         status.name().substring(1).toLowerCase();
        
        this.canCreateOrder = status == TableStatus.AVAILABLE || status == TableStatus.RESERVED;
        this.canEditOrder = status == TableStatus.OCCUPIED;
        this.canMarkAvailable = status == TableStatus.OCCUPIED;
    }

    public Long getId() {
        return table.getId();
    }

    public Integer getNumber() {
        return table.getNumber();
    }

    public Integer getCapacity() {
        return table.getCapacity();
    }

    public String getStatusClass() {
        return statusClass;
    }

    public String getStatusBadge() {
        return statusBadge;
    }

    public String getStatusText() {
        return statusText;
    }

    public boolean isCanCreateOrder() {
        return canCreateOrder;
    }

    public boolean isCanEditOrder() {
        return canEditOrder;
    }

    public boolean isCanMarkAvailable() {
        return canMarkAvailable;
    }
} 