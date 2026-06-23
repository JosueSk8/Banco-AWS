import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class BancoCore {

    private final ConcurrentHashMap<String, Cuenta> cuentasDB = new ConcurrentHashMap<>();
    
    // Variables seguras para concurrencia masiva (Métricas)
    private double saldoTotalGlobal = 0.0;
    private final AtomicLong totalTransferencias = new AtomicLong(0);
    private final AtomicReference<String> ultimaTxId = new AtomicReference<>("Ninguna");

    public void inicializarBaseDeDatos() {
        System.out.println("Arrancando motor bancario en memoria (Java Puro)...");

        ArrayList<String> nombres = leerArchivo("nombres.txt");
        ArrayList<String> apellidos = leerArchivo("apellidos.txt");

        Random random = new Random(12345);
        int idCounter = 1;
        double sumaTemporal = 0.0;

        for (String nombre : nombres) {
            for (String ap1 : apellidos) {
                for (String ap2 : apellidos) {
                    double numero = random.nextDouble() * 10000000;
                    numero = Math.round(numero * 10) / 100.0;

                    String id = String.valueOf(idCounter++);
                    String nombreCompleto = nombre + " " + ap1 + " " + ap2;
                    
                    cuentasDB.put(id, new Cuenta(id, nombreCompleto, numero));
                    sumaTemporal += numero; // Sumamos el dinero total del banco
                }
            }
        }
        this.saldoTotalGlobal = sumaTemporal;
        System.out.println("Base de datos cargada. Total de cuentas: " + cuentasDB.size());
    }

    private ArrayList<String> leerArchivo(String nombreArchivo) {
        ArrayList<String> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    lista.add(linea);
                }
            }
        } catch (Exception e) {
            System.out.println("Error leyendo archivo: " + nombreArchivo);
        }
        return lista;
    }

    public Cuenta obtenerCuenta(String id) {
        return cuentasDB.get(id);
    }

    public boolean transferir(String fromId, String toId, double monto) {
        if (fromId.equals(toId) || monto <= 0) return false;
        
        Cuenta origen = cuentasDB.get(fromId);
        Cuenta destino = cuentasDB.get(toId);
        
        if (origen == null || destino == null) return false;

        Cuenta primerLock = fromId.compareTo(toId) < 0 ? origen : destino;
        Cuenta segundoLock = fromId.compareTo(toId) < 0 ? destino : origen;

        synchronized (primerLock) {
            synchronized (segundoLock) {
                if (origen.getBalance() >= monto) {
                    origen.retirar(monto);
                    destino.depositar(monto);
                    
                    // Actualizamos las métricas de forma segura
                    totalTransferencias.incrementAndGet();
                    ultimaTxId.set("TX-" + (System.currentTimeMillis() % 100000));
                    
                    return true;
                }
                return false;
            }
        }
    }

    // Getters para que el MetricsHandler lea los datos
    public double getSaldoTotalGlobal() { return saldoTotalGlobal; }
    public long getTotalTransferencias() { return totalTransferencias.get(); }
    public String getUltimaTxId() { return ultimaTxId.get(); }
}