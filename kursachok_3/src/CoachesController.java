// CoachesController.java
import java.sql.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.logging.*;

public class CoachesController {
    private static final Logger logger = Logger.getLogger(CoachesController.class.getName());

    public static JsonObject addCoach(JsonObject data) {
        JsonObject response = new JsonObject();
        String name = data.get("name").getAsString();
        String specialization = data.get("specialization").getAsString();
        int experience = data.get("experience").getAsInt();

        String sql = "INSERT INTO Coaches (name, specialization, experience) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, specialization);
            pstmt.setInt(3, experience);
            pstmt.executeUpdate();

            response.addProperty("status", "success");
            response.addProperty("message", "Тренер добавлен успешно");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении тренера", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при добавлении тренера");
        }

        return response;
    }

    public static JsonObject getCoaches() {
        JsonObject response = new JsonObject();
        JsonArray coachesList = new JsonArray();

        String sql = "SELECT * FROM Coaches";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JsonObject coach = new JsonObject();
                coach.addProperty("id", rs.getInt("id"));
                coach.addProperty("name", rs.getString("name"));
                coach.addProperty("specialization", rs.getString("specialization"));
                coach.addProperty("experience", rs.getInt("experience"));
                coachesList.add(coach);
            }

            response.addProperty("status", "success");
            response.add("data", coachesList);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при получении тренеров", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при получении тренеров");
        }

        return response;
    }

    public static JsonObject updateCoach(JsonObject data) {
        JsonObject response = new JsonObject();
        int id = data.get("id").getAsInt();
        String name = data.get("name").getAsString();
        String specialization = data.get("specialization").getAsString();
        int experience = data.get("experience").getAsInt();

        String sql = "UPDATE Coaches SET name = ?, specialization = ?, experience = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, specialization);
            pstmt.setInt(3, experience);
            pstmt.setInt(4, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.addProperty("status", "success");
                response.addProperty("message", "Тренер обновлен успешно");
            } else {
                response.addProperty("status", "error");
                response.addProperty("message", "Тренер не найден");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении тренера", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при обновлении тренера");
        }

        return response;
    }

    public static JsonObject deleteCoach(JsonObject data) {
        JsonObject response = new JsonObject();
        int id = data.get("id").getAsInt();

        String sql = "DELETE FROM Coaches WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.addProperty("status", "success");
                response.addProperty("message", "Тренер удален успешно");
            } else {
                response.addProperty("status", "error");
                response.addProperty("message", "Тренер не найден");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при удалении тренера", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при удалении тренера");
        }

        return response;
    }

    public static JsonObject searchCoaches(JsonObject data) {
        JsonObject response = new JsonObject();
        String keyword = data.get("keyword").getAsString();

        String sql = "SELECT * FROM Coaches WHERE specialization ILIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            JsonArray coachesList = new JsonArray();
            while (rs.next()) {
                JsonObject coach = new JsonObject();
                coach.addProperty("id", rs.getInt("id"));
                coach.addProperty("name", rs.getString("name"));
                coach.addProperty("specialization", rs.getString("specialization"));
                coach.addProperty("experience", rs.getInt("experience"));
                coachesList.add(coach);
            }

            response.addProperty("status", "success");
            response.add("data", coachesList);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при поиске тренеров", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при поиске тренеров");
        }

        return response;
    }
}
