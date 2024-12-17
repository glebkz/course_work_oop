// DatabaseConnection.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Параметры подключения
    private static final String URL = "jdbc:postgresql://localhost:5432/sportik_club";
    private static final String USER = "fito_nyasha"; // Имя пользователя
    private static final String PASSWORD = "123456"; // Пароль пользователя

    // Метод для получения соединения
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}