import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SyncHandler implements HttpHandler {
    
    private final BancoCore bancoCore;

    public SyncHandler(BancoCore bancoCore) {
        this.bancoCore = bancoCore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            try {
                String source = extractJsonValue(body, "sourceAccountId");
                String target = extractJsonValue(body, "targetAccountId");
                double amount = Double.parseDouble(extractJsonValue(body, "amount"));

                // Ejecutamos la transferencia en la memoria de la réplica
                bancoCore.transferir(source, target, amount);
                
                // Respondemos éxito sin llamar al ReplicadorNodos
                sendResponse(exchange, 200, "{\"status\": \"Sincronizado\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"error\": \"Error al sincronizar\"}");
            }
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Metodo no permitido\"}");
        }
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;
        int colonIndex = json.indexOf(":", keyIndex);
        int commaIndex = json.indexOf(",", colonIndex);
        int braceIndex = json.indexOf("}", colonIndex);
        int endIndex = (commaIndex != -1 && commaIndex < braceIndex) ? commaIndex : braceIndex;
        String value = json.substring(colonIndex + 1, endIndex).trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}