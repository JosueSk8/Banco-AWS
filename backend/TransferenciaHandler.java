import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TransferenciaHandler implements HttpHandler {
    
    private final BancoCore bancoCore;

    public TransferenciaHandler(BancoCore bancoCore) {
        this.bancoCore = bancoCore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // --- CORS ---
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Petición preflight del navegador
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            try {
                String source = extractJsonValue(body, "sourceAccountId");
                String target = extractJsonValue(body, "targetAccountId");
                double amount = Double.parseDouble(extractJsonValue(body, "amount"));

                // Intentamos hacer la transferencia en memoria
                //Si es exitosa, BancoCore ahora dispara automáticamente SNS y S3
                boolean exito = bancoCore.transferir(source, target, amount);

                if (exito) {
                    sendResponse(exchange, 200, "{\"mensaje\": \"Transferencia exitosa\"}");
                } else {
                    sendResponse(exchange, 400, "{\"error\": \"Fondos insuficientes o cuentas invalidas\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"error\": \"Formato JSON incorrecto\"}");
            }
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Metodo no permitido\"}");
        }
    }

    // Extractor manual de JSON
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

    // Respuesta genérica
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}