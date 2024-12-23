// src/main/java/com/example/dao/CoachDAO.java
package com.example.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.example.server.*;
import com.example.client.model.Coach;
import com.example.client.model.enums.Specialization;

public class CoachDAO {
    private final Connection connection;

    public CoachDAO() {
        this.connection = Database.getConnection();
    }

    // Создание нового тренера
    public Coach create(Coach coach) throws SQLException {
        String sql = "INSERT INTO coaches (name, specialization, experience_years) " +
                     "VALUES (?, ?, ?) RETURNING id;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, coach.getName());
            pstmt.setString(2, coach.getSpecialization().name()); // Предполагается, что Specialization - enum
            pstmt.setInt(3, coach.getExperienceYears());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                coach.setId(rs.getInt("id"));
            }
        }
        return coach;
    }

    // Получение тренера по ID
    public Coach getById(int id) throws SQLException {
        String sql = "SELECT * FROM coaches WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Coach coach = new Coach();
                coach.setId(rs.getInt("id"));
                coach.setName(rs.getString("name"));
                coach.setSpecialization(Specialization.valueOf(rs.getString("specialization").toUpperCase()));
                coach.setExperienceYears(rs.getInt("experience_years"));
                return coach;
            }
        }
        return null;
    }

    // Получение всех тренеров
    public List<Coach> getAll() throws SQLException {
        String sql = "SELECT * FROM coaches;";
        List<Coach> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Coach coach = new Coach();
                coach.setId(rs.getInt("id"));
                coach.setName(rs.getString("name"));
                coach.setSpecialization(Specialization.valueOf(rs.getString("specialization").toUpperCase()));
                coach.setExperienceYears(rs.getInt("experience_years"));
                result.add(coach);
            }
        }
        return result;
    }

    // Обновление тренера
    public boolean update(Coach coach) throws SQLException {
        String sql = "UPDATE coaches SET name = ?, specialization = ?, experience_years = ? WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, coach.getName());
            pstmt.setString(2, coach.getSpecialization().name());
            pstmt.setInt(3, coach.getExperienceYears());
            pstmt.setInt(4, coach.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Удаление тренера по ID
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM coaches WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}