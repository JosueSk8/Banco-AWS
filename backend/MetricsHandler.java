import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
            
            // Instanciamos el lector nativo del Sistema Operativo (Ubuntu)
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            // Calculo de RAM Real del servidor
            long totalRam = osBean.getTotalMemorySize();
            long freeRam = osBean.getFreeMemorySize();
            double ramPercent = ((double) (totalRam - freeRam) / totalRam) * 100;

            // Calculo de CPU del servidor (
            double cpuPercent = osBean.getCpuLoad() * 100;
            if (cpuPercent < 0) cpuPercent = 0.0; // Previene el -1 inicial

            // 4. Calculo de Disco Raiz (/)
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            double diskPercent = ((double) (totalSpace - freeSpace) / totalSpace) * 100;

            // Formateamos el JSON (usamos Locale.US para que los decimales sean con punto y no rompan el JSON)
            String json = String.format(
                Locale.US,
                "{\n  \"estado\": \"Activo\",\n  \"cuentas\": 820000,\n  \"saldoTotal\": %.2f,\n  \"transferencias\": %d,\n  \"ultimaTx\": \"%s\",\n  \"ramUsoPorcentaje\": %.2f,\n  \"cpuUsoPorcentaje\": %.2f,\n  \"discoUsoPorcentaje\": %.2f\n}",
                bancoCore.getSaldoTotalGlobal(),
                bancoCore.getTotalTransferencias(),
                bancoCore.getUltimaTxId(),
                ramPercent,
                cpuPercent,
                diskPercent
            );

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            
            byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}