// src/main/java/com/example/client/model/Equipment.java
package com.example.client.model;

import com.example.client.model.enums.EquipmentType;
import com.example.client.model.enums.EquipmentStatus;

/**
 * Класс модели для оборудования.
 */
public class Equipment {
    private int id;
    private String name;
    private EquipmentType type;
    private int quantity;
    private EquipmentStatus status;

    // Конструкторы
    public Equipment() {
    }

    public Equipment(int id, String name, EquipmentType type, int quantity, EquipmentStatus status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.status = status;
    }

    // Геттеры и Сеттеры

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    } 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    } 

    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    } 

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    } 

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }
}