// ClientConnection.java
import java.io.*;
import java.net.Socket;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ClientConnection {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson = new Gson();

    public ClientConnection() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public JsonObject sendRequest(String action, JsonObject data) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", action);
        request.add("data", data);
        out.println(gson.toJson(request));

        String responseStr = in.readLine();
        return gson.fromJson(responseStr, JsonObject.class);
    }

    public void close() throws IOException {
        socket.close();
    }
}
