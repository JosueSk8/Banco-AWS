import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. Inicializamos tu motor en memoria 
        BancoCore bancoCore = new BancoCore();
        bancoCore.inicializarBaseDeDatos();

        // 2. Levantamos el servidor HTTP nativo de Java en el puerto 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 3. Creamos las rutas (Endpoints)
        // El handler se encargará de recibir la petición y devolver el JSON
        server.createContext("/api/accounts", new CuentasHandler(bancoCore));
        server.createContext("/api/transactions/transfer", new TransferenciaHandler(bancoCore));

        // 4. Configuramos el servidor para que use un pool de hilos y soporte concurrencia masiva
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        
        System.out.println("Servidor nativo iniciado en el puerto 8080...");
        server.start();
    }
}