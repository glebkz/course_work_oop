// src/main/java/com/example/server/Database.java
package com.example.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static Connection connection;

    // Инициализация базы данных
    public static void init() {
        String url = "jdbc:postgresql://localhost:5432/sports_db";
        String user = "myuser";
        String password = "123456";

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Подключение к базе данных успешно.");

            // Создание таблиц, если они не существуют
            createTables();

            System.out.println("Таблицы успешно созданы или уже существуют.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Создание необходимых таблиц
    private static void createTables() throws SQLException {
        String createEquipmentTable = "CREATE TABLE IF NOT EXISTS equipment (" +
                "id SERIAL PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "type VARCHAR(255) NOT NULL," +
                "quantity INT NOT NULL," +
                "status VARCHAR(255) NOT NULL" +
                ");";

        String createCoachTable = "CREATE TABLE IF NOT EXISTS coaches (" +
                "id SERIAL PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "specialization VARCHAR(255) NOT NULL," +
                "experience_years INT NOT NULL" +
                ");";

        // Удаляем equipment_id из таблицы athletes
        String createAthleteTable = "CREATE TABLE IF NOT EXISTS athletes (" +
                "id SERIAL PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "age INT NOT NULL," +
                "rank VARCHAR(255) NOT NULL," +
                "coach_id INT," +
                "FOREIGN KEY (coach_id) REFERENCES coaches(id)" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createEquipmentTable);
            stmt.execute(createCoachTable);
            stmt.execute(createAthleteTable);
        }
    }

    // Получение соединения с базой данных
    public static Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("База данных не инициализирована. Вызовите Database.init() перед использованием.");
        }
        return connection;
    }
}