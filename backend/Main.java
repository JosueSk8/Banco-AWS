import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        
        // 1. Inicializamos el motor y los 820,000 registros
        BancoCore bancoCore = new BancoCore();
        bancoCore.inicializarBaseDeDatos();

        // 2. Creamos el servidor en el puerto 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 3. Registramos los Endpoints para el Dashboard
        server.createContext("/api/accounts", new CuentasHandler(bancoCore));
        server.createContext("/api/transactions/transfer", new TransferenciaHandler(bancoCore));
        server.createContext("/api/metrics", new MetricsHandler(bancoCore));
        
        // 4. Registramos el Endpoint interno para la replicación
        server.createContext("/api/internal/sync", new SyncHandler(bancoCore));

        // 5. Configuramos la alberca de hilos para aguantar tráfico pesado
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        
        System.out.println("Servidor nativo iniciado en el puerto 8080...");
        server.start();
    }
}