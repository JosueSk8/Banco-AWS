import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class CuentasHandler implements HttpHandler {
    
    private final BancoCore bancoCore;

    public CuentasHandler(BancoCore bancoCore) {
        this.bancoCore = bancoCore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            
            // Extraer el ID de la ruta /api/accounts/123
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            
            if (parts.length == 4) {
                String id = parts[3];
                Cuenta cuenta = bancoCore.obtenerCuenta(id);

                if (cuenta != null) {
                    // Armamos el JSON manualmente
                    String jsonResponse = String.format(
                        "{\n  \"id\": \"%s\",\n  \"propietario\": \"%s\",\n  \"balance\": %.2f\n}",
                        cuenta.getId(), cuenta.getPropietario(), cuenta.getBalance()
                    );
                    sendResponse(exchange, 200, jsonResponse);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Cuenta no encontrada\"}"); // 404 Not Found
                }
            } else {
                sendResponse(exchange, 400, "{\"error\": \"ID invalido\"}"); // 400 Bad Request
            }
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Metodo no permitido\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}