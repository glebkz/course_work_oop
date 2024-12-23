// src/main/java/com/example/dao/EquipmentDAO.java
package com.example.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.example.server.*;
import com.example.client.model.Equipment;
import com.example.client.model.enums.*;

public class EquipmentDAO {
    private final Connection connection;

    public EquipmentDAO() {
        this.connection = Database.getConnection();
    }

    // Создание нового оборудования
    public Equipment create(Equipment eq) throws SQLException {
        String sql = "INSERT INTO equipment (name, type, quantity, status) " +
                     "VALUES (?, ?, ?, ?) RETURNING id;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, eq.getName());
            pstmt.setString(2, eq.getType().name()); // Предполагается, что EquipmentType - enum
            pstmt.setInt(3, eq.getQuantity());
            pstmt.setString(4, eq.getStatus().name()); // Предполагается, что EquipmentStatus - enum

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                eq.setId(rs.getInt("id"));
            }
        }
        return eq;
    }

    // Получение оборудования по ID
    public Equipment getById(int id) throws SQLException {
        String sql = "SELECT * FROM equipment WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Equipment eq = new Equipment();
                eq.setId(rs.getInt("id"));
                eq.setName(rs.getString("name"));
                eq.setType(EquipmentType.valueOf(rs.getString("type").toUpperCase()));
                eq.setQuantity(rs.getInt("quantity"));
                eq.setStatus(EquipmentStatus.valueOf(rs.getString("status").toUpperCase()));
                return eq;
            }
        }
        return null;
    }

    // Получение всего оборудования
    public List<Equipment> getAll() throws SQLException {
        String sql = "SELECT * FROM equipment;";
        List<Equipment> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Equipment eq = new Equipment();
                eq.setId(rs.getInt("id"));
                eq.setName(rs.getString("name"));
                eq.setType(EquipmentType.valueOf(rs.getString("type").toUpperCase()));
                eq.setQuantity(rs.getInt("quantity"));
                eq.setStatus(EquipmentStatus.valueOf(rs.getString("status").toUpperCase()));
                result.add(eq);
            }
        }
        return result;
    }

    // Обновление оборудования
    public boolean update(Equipment eq) throws SQLException {
        String sql = "UPDATE equipment SET name = ?, type = ?, quantity = ?, status = ? WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, eq.getName());
            pstmt.setString(2, eq.getType().name());
            pstmt.setInt(3, eq.getQuantity());
            pstmt.setString(4, eq.getStatus().name());
            pstmt.setInt(5, eq.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Удаление оборудования по ID
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM equipment WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}