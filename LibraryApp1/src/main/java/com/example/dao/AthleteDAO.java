// src/main/java/com/example/dao/AthleteDAO.java
package com.example.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.example.server.*;
import com.example.client.model.Athlete;
import com.example.client.model.enums.Rank;

public class AthleteDAO {
    private final Connection connection;

    public AthleteDAO() {
        this.connection = Database.getConnection();
    }

    // Создание нового спортсмена
    public Athlete create(Athlete athlete) throws SQLException {
        String sql = "INSERT INTO athletes (name, age, rank, coach_id) " +
                     "VALUES (?, ?, ?, ?) RETURNING id;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, athlete.getName());
            pstmt.setInt(2, athlete.getAge());
            pstmt.setString(3, athlete.getRank().name()); // Предполагается, что Rank - enum
            if (athlete.getCoachId() != 0) {
                pstmt.setInt(4, athlete.getCoachId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                athlete.setId(rs.getInt("id"));
            }
        }
        return athlete;
    }

    // Получение спортсмена по ID
    public Athlete getById(int id) throws SQLException {
        String sql = "SELECT * FROM athletes WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Athlete athlete = new Athlete();
                athlete.setId(rs.getInt("id"));
                athlete.setName(rs.getString("name"));
                athlete.setAge(rs.getInt("age"));
                athlete.setRank(Rank.valueOf(rs.getString("rank").toUpperCase())); // Предполагается, что Rank - enum
                athlete.setCoachId(rs.getInt("coach_id"));
                return athlete;
            }
        }
        return null;
    }

    // Получение всех спортсменов
    public List<Athlete> getAll() throws SQLException {
        String sql = "SELECT * FROM athletes;";
        List<Athlete> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Athlete athlete = new Athlete();
                athlete.setId(rs.getInt("id"));
                athlete.setName(rs.getString("name"));
                athlete.setAge(rs.getInt("age"));
                athlete.setRank(Rank.valueOf(rs.getString("rank").toUpperCase()));
                athlete.setCoachId(rs.getInt("coach_id"));
                result.add(athlete);
            }
        }
        return result;
    }

    // Обновление спортсмена
    public boolean update(Athlete athlete) throws SQLException {
        String sql = "UPDATE athletes SET name = ?, age = ?, rank = ?, coach_id = ? WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, athlete.getName());
            pstmt.setInt(2, athlete.getAge());
            pstmt.setString(3, athlete.getRank().name());
            if (athlete.getCoachId() != 0) {
                pstmt.setInt(4, athlete.getCoachId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, athlete.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Удаление спортсмена по ID
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM athletes WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}