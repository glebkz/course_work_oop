// InventoryController.java
import java.sql.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class InventoryController {

    public static JsonObject addInventory(JsonObject data) {
        JsonObject response = new JsonObject();
        String name = data.get("name").getAsString();
        String type = data.get("type").getAsString();
        int quantity = data.get("quantity").getAsInt();
        String status = data.get("status").getAsString();

        String sql = "INSERT INTO Inventory (name, type, quantity, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, status);
            pstmt.executeUpdate();

            response.addProperty("status", "success");
            response.addProperty("message", "Инвентарь добавлен успешно");
        } catch (SQLException e) {
            e.printStackTrace();
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при добавлении инвентаря");
        }

        return response;
    }

    public static JsonObject getInventory() {
        JsonObject response = new JsonObject();
        JsonArray inventoryList = new JsonArray();

        String sql = "SELECT * FROM Inventory";
        System.out.println("Выполняется запрос: " + sql); // Лог запроса

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Соединение с БД успешно установлено.");
            while (rs.next()) {
                JsonObject inventory = new JsonObject();
                inventory.addProperty("id", rs.getInt("id"));
                inventory.addProperty("name", rs.getString("name"));
                inventory.addProperty("type", rs.getString("type"));
                inventory.addProperty("quantity", rs.getInt("quantity"));
                inventory.addProperty("status", rs.getString("status"));
                inventoryList.add(inventory);
            }

            response.addProperty("status", "success");
            response.add("data", inventoryList);
            System.out.println("Данные успешно получены: " + inventoryList.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при получении инвентаря");
            System.err.println("Ошибка SQL: " + e.getMessage());
        }

        return response;
    }


    public static JsonObject updateInventory(JsonObject data) {
        JsonObject response = new JsonObject();
        int id = data.get("id").getAsInt();
        String name = data.get("name").getAsString();
        String type = data.get("type").getAsString();
        int quantity = data.get("quantity").getAsInt();
        String status = data.get("status").getAsString();

        String sql = "UPDATE Inventory SET name = ?, type = ?, quantity = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, status);
            pstmt.setInt(5, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.addProperty("status", "success");
                response.addProperty("message", "Инвентарь обновлен успешно");
            } else {
                response.addProperty("status", "error");
                response.addProperty("message", "Инвентарь не найден");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при обновлении инвентаря");
        }

        return response;
    }

    public static JsonObject deleteInventory(JsonObject data) {
        JsonObject response = new JsonObject();
        int id = data.get("id").getAsInt();

        String sql = "DELETE FROM Inventory WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.addProperty("status", "success");
                response.addProperty("message", "Инвентарь удален успешно");
            } else {
                response.addProperty("status", "error");
                response.addProperty("message", "Инвентарь не найден");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.addProperty("status", "error");
            response.addProperty("message", "Ошибка при удалении инвентаря");
        }

        return response;
    }
}
