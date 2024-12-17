// AthletesController.java
import java.sql.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.logging.*;

public class AthletesController {
    private static final Logger logger = Logger.getLogger(AthletesController.class.getName());

    public static JsonObject addAthlete(JsonObject data) {
        JsonObject response = new JsonObject();
        String name = data.get("name").getAsString();
        int age = data.get("age").getAsInt();
        String rank = data.get("rank").getAsString();
        int coachId = data.has("coach_id") && !data.get("coach_id").isJsonNull() ? data.get("coach_id").getAsInt() : 0;

        String sql = "INSERT INTO Athletes (name, age, rank, coach_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, rank);
            if (coachId > 0) {
                pstmt.setInt(4, coachId);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.executeUpdate();

            response.addProperty("status", "success");
            response.addProperty("message", "Спортсмен добавлен успешно");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении спортсмена", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при добавлении спортсмена");
        }

        return response;
    }

    public static JsonObject getAthletes() {
        JsonObject response = new JsonObject();
        JsonArray athletesList = new JsonArray();

        String sql = "SELECT Athletes.*, Coaches.name AS coach_name FROM Athletes LEFT JOIN Coaches ON Athletes.coach_id = Coaches.id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JsonObject athlete = new JsonObject();
                athlete.addProperty("id", rs.getInt("id"));
                athlete.addProperty("name", rs.getString("name"));
                athlete.addProperty("age", rs.getInt("age"));
                athlete.addProperty("rank", rs.getString("rank"));
                if (rs.getObject("coach_id") != null) {
                    athlete.addProperty("coach_id", rs.getInt("coach_id"));
                    athlete.addProperty("coach_name", rs.getString("coach_name"));
                } else {
                    athlete.addProperty("coach_id", (String) null);
                    athlete.addProperty("coach_name", (String) null);
                }
                athletesList.add(athlete);
            }

            response.addProperty("status", "success");
            response.add("data", athletesList);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при получении спортсменов", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при получении спортсменов");
        }

        return response;
    }

    public static JsonObject updateAthlete(JsonObject data) {
        JsonObject response = new JsonObject();
        int id = data.get("id").getAsInt();
        String name = data.get("name").getAsString();
        int age = data.get("age").getAsInt();
        String rank = data.get("rank").getAsString();
        int coachId = data.has("coach_id") && !data.get("coach_id").isJsonNull() ? data.get("coach_id").getAsInt() : 0;

        String sql = "UPDATE Athletes SET name = ?, age = ?, rank = ?, coach_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, rank);
            if (coachId > 0) {
                pstmt.setInt(4, coachId);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.addProperty("status", "success");
                response.addProperty("message", "Спортсмен обновлен успешно");
            } else {
                response.addProperty("status", "error");
                response.addProperty("message", "Спортсмен не найден");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении спортсмена", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при обновлении спортсмена");
        }

        return response;
    }

    public static JsonObject deleteAthlete(JsonObject data) {
        JsonObject response = new JsonObject();
        int id = data.get("id").getAsInt();

        String sql = "DELETE FROM Athletes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.addProperty("status", "success");
                response.addProperty("message", "Спортсмен удален успешно");
            } else {
                response.addProperty("status", "error");
                response.addProperty("message", "Спортсмен не найден");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при удалении спортсмена", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при удалении спортсмена");
        }

        return response;
    }

    public static JsonObject searchAthletes(JsonObject data) {
        JsonObject response = new JsonObject();
        String keyword = data.get("keyword").getAsString();

        String sql = "SELECT Athletes.*, Coaches.name AS coach_name FROM Athletes LEFT JOIN Coaches ON Athletes.coach_id = Coaches.id " +
                "WHERE Athletes.name ILIKE ? OR Athletes.rank ILIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            JsonArray athletesList = new JsonArray();
            while (rs.next()) {
                JsonObject athlete = new JsonObject();
                athlete.addProperty("id", rs.getInt("id"));
                athlete.addProperty("name", rs.getString("name"));
                athlete.addProperty("age", rs.getInt("age"));
                athlete.addProperty("rank", rs.getString("rank"));
                if (rs.getObject("coach_id") != null) {
                    athlete.addProperty("coach_id", rs.getInt("coach_id"));
                    athlete.addProperty("coach_name", rs.getString("coach_name"));
                } else {
                    athlete.addProperty("coach_id", (String) null);
                    athlete.addProperty("coach_name", (String) null);
                }
                athletesList.add(athlete);
            }

            response.addProperty("status", "success");
            response.add("data", athletesList);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при поиске спортсменов", e);
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при поиске спортсменов");
        }

        return response;
    }
}
