import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ReplicadorNodos {
    
    // Aquí pondremos las IPs públicas de AWS más adelante.
    // Por ahora usamos URLs de prueba (imaginando que las réplicas correrían en puertos 8081 y 8082).
    private static final String[] REPLICAS = {
        "http://localhost:8081/api/internal/sync",
        "http://localhost:8082/api/internal/sync"
    };

    public static void propagar(String source, String target, double amount) {
        // Armamos el JSON de forma manual
        String jsonPayload = String.format(
            "{\"sourceAccountId\":\"%s\",\"targetAccountId\":\"%s\",\"amount\":%s}", 
            source, target, amount
        );

        for (String replicaUrl : REPLICAS) {
            // Disparamos la petición en un hilo en segundo plano
            new Thread(() -> {
                try {
                    URL url = new URL(replicaUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(2000); // Si un nodo se cae, no nos quedamos esperando por siempre

                    try(OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        System.out.println("Aviso: La replica en " + replicaUrl + " no respondio con 200 OK.");
                    }
                } catch (Exception e) {
                    System.out.println("Aviso: Nodo inalcanzable -> " + replicaUrl);
                }
            }).start();
        }
    }
}