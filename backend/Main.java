import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        
        if (args.length < 1) {
            System.out.println("Error: Debes especificar el rol del nodo. Ejemplo: ./gradlew run --args='leader'");
            System.exit(1);
        }

        String rol = args[0].toLowerCase();
        
        // Inicializamos el motor en RAM (Las 3 máquinas hacen esto)
        BancoCore bancoCore = new BancoCore();
        bancoCore.inicializarBaseDeDatos();
        if (rol.equals("leader")) {
            System.out.println("Levantando NODO LÍDER...");
            
            // Inyectamos las capacidades de Nube al Banco
            bancoCore.setS3Logger(new S3Logger());
            bancoCore.setSnsPublisher(new SnsPublisher());

            // Levantamos el servidor HTTP para recibir tráfico
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api/accounts", new CuentasHandler(bancoCore));
            server.createContext("/api/transactions/transfer", new TransferenciaHandler(bancoCore));
            server.createContext("/api/metrics", new MetricsHandler(bancoCore));
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            System.out.println("Servidor HTTP nativo iniciado en el puerto 8080...");

        } else if (rol.startsWith("replica")) {
            // Ejemplo: replica2 o replica3
            String nombreCola = rol.equals("replica2") ? "Cola-Nodo2" : "Cola-Nodo3";
            System.out.println("Levantando RÉPLICA escuchando en " + nombreCola + "...");
            
            // 1. Recuperación ante desastres (Disaster Recovery)
            S3Logger s3Logger = new S3Logger();
            long ultimaTxLocal = bancoCore.getTotalTransferencias(); 
            s3Logger.recuperarDesdeS3(ultimaTxLocal, bancoCore);
            
            // 2. Encendemos el oyente de SQS en un hilo de fondo
            SqsListener listener = new SqsListener(nombreCola, bancoCore);
            new Thread(listener).start();
            
            // Opcional: Levantar el servidor HTTP en las réplicas SOLO para leer métricas
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api/metrics", new MetricsHandler(bancoCore));
            server.setExecutor(null);
            server.start();
        } else {
            System.out.println("Rol no válido. Usa 'leader', 'replica2' o 'replica3'.");
        }
    }
}