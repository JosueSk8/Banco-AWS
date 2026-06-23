import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class MetricsHandler implements HttpHandler {
    
    private final BancoCore bancoCore;

    public MetricsHandler(BancoCore bancoCore) {
        this.bancoCore = bancoCore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // --- CORS ---
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equals(exchange.getRequestMethod())) {
            Runtime runtime = Runtime.getRuntime();
            long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
            double ramPercent = ((double) memoryUsed / runtime.totalMemory()) * 100;

            String json = String.format(
                "{\n  \"estado\": \"Activo\",\n  \"cuentas\": 820000,\n  \"saldoTotal\": %.2f,\n  \"transferencias\": %d,\n  \"ultimaTx\": \"%s\",\n  \"ramUsoPorcentaje\": %.2f\n}",
                bancoCore.getSaldoTotalGlobal(),
                bancoCore.getTotalTransferencias(),
                bancoCore.getUltimaTxId(),
                ramPercent
            );

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(json.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}