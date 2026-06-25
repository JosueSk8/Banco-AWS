import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MetricsHandler implements HttpHandler {

    private final BancoCore bancoCore;

    // --- IPS PRIVADAS 
    private static final String IP_PRIVADA_REP2 = "172.31.43.72";
    private static final String IP_PRIVADA_REP3 = "172.31.47.163";

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

            // OBTENER MÉTRICAS DEL LÍDER
            String metricasLider = obtenerMetricasLocales();

            // OBTENER MÉTRICAS DE LAS RÉPLICAS (Peticiones HTTP internas)
            String metricasRep2 = obtenerMetricasRemotas(IP_PRIVADA_REP2);
            String metricasRep3 = obtenerMetricasRemotas(IP_PRIVADA_REP3);

            // CONSTRUIR EL SÚPER-JSON
            String jsonConsolidado = String.format(
                "{\n" +
                "  \"lider\": %s,\n" +
                "  \"replica2\": %s,\n" +
                "  \"replica3\": %s\n" +
                "}",
                metricasLider, metricasRep2, metricasRep3
            );

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonConsolidado.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    // Método para leer CPU/RAM/Disco físicos del servidor donde corre este código
    private String obtenerMetricasLocales() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long totalRam = osBean.getTotalMemorySize();
        long freeRam = osBean.getFreeMemorySize();
        double ramPercent = ((double) (totalRam - freeRam) / totalRam) * 100;

        double cpuPercent = osBean.getCpuLoad() * 100;
        if (cpuPercent < 0) cpuPercent = 0.0;

        File root = new File("/");
        long totalSpace = root.getTotalSpace();
        long freeSpace = root.getFreeSpace();
        double diskPercent = ((double) (totalSpace - freeSpace) / totalSpace) * 100;

        return String.format(
            Locale.US,
            "{\"cuentas\": 820000, \"saldoTotal\": %.2f, \"transferencias\": %d, \"ultimaTx\": \"%s\", \"ramUsoPorcentaje\": %.2f, \"cpuUsoPorcentaje\": %.2f, \"discoUsoPorcentaje\": %.2f}",
            bancoCore.getSaldoTotalGlobal(),
            bancoCore.getTotalTransferencias(),
            bancoCore.getUltimaTxId(),
            ramPercent,
            cpuPercent,
            diskPercent
        );
    }

    // Método que el Líder usa para preguntarle a las réplicas
    private String obtenerMetricasRemotas(String ipPrivada) {
        try {
            // El líder le pide a la réplica una versión "limpia" de las métricas
            URL url = new URL("http://" + ipPrivada + ":8080/api/metrics/local");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(2000); // No esperar más de 2 segundos
            con.setReadTimeout(2000);

            if (con.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();
                return content.toString(); // Retorna el JSON crudo de la réplica
            }
        } catch (Exception e) {
            // Si la réplica está apagada, devolvemos valores en cero
            System.out.println("No se pudo conectar a la réplica " + ipPrivada);
        }
        return "{\"cuentas\": 0, \"saldoTotal\": 0, \"transferencias\": 0, \"ultimaTx\": \"Desconectado\", \"ramUsoPorcentaje\": 0.0, \"cpuUsoPorcentaje\": 0.0, \"discoUsoPorcentaje\": 0.0}";
    }
}