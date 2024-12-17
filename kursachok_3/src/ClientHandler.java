// ClientHandler.java
import java.io.*;
import java.net.Socket;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Gson gson = new Gson();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        clientSocket.getOutputStream(), true);
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Обработка полученного запроса
                JsonObject request = gson.fromJson(inputLine, JsonObject.class);
                String action = request.get("action").getAsString();
                JsonObject data = request.getAsJsonObject("data");

                JsonObject response = new JsonObject();

                switch (action) {
                    case "addInventory":
                        response = InventoryController.addInventory(data);
                        break;
                    case "getInventory":
                        response = InventoryController.getInventory();
                        break;
                    case "updateInventory":
                        response = InventoryController.updateInventory(data);
                        break;
                    case "deleteInventory":
                        response = InventoryController.deleteInventory(data);
                        break;
                    // Аналогичные кейсы для Coaches и Athletes
                    default:
                        response.addProperty("status", "error");
                        response.addProperty("message", "Неизвестное действие");
                }

                // Отправка ответа клиенту
                out.println(gson.toJson(response));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
