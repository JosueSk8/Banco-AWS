package com.example.backend;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BancoCore {

    private final ConcurrentHashMap<String, Cuenta> cuentasDB = new ConcurrentHashMap<>();

    @PostConstruct
    public void inicializarBaseDeDatos() {
        System.out.println("Arrancando motor bancario en memoria...");

        ArrayList<String> nombres = leerArchivo("nombres.txt");
        ArrayList<String> apellidos = leerArchivo("apellidos.txt");

        Random random = new Random(12345);
        int idCounter = 1;

        for (String nombre : nombres) {
            for (String ap1 : apellidos) {
                for (String ap2 : apellidos) {

                    double numero = random.nextDouble() * 10000000;
                    numero = Math.round(numero * 10) / 100.0;

                    String id = String.valueOf(idCounter++);
                    String nombreCompleto = nombre + " " + ap1 + " " + ap2;

                    cuentasDB.put(id, new Cuenta(id, nombreCompleto, numero));
                }
            }
        }

        System.out.println("Base de datos cargada. Total de cuentas: " + cuentasDB.size());
    }

    private ArrayList<String> leerArchivo(String nombreArchivo) {
        ArrayList<String> lista = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(nombreArchivo)))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    lista.add(linea);
                }
            }

        } catch (Exception e) {
            System.out.println("Error leyendo archivo: " + nombreArchivo);
            e.printStackTrace();
        }

        return lista;
    }

    public Cuenta obtenerCuenta(String id) {
        return cuentasDB.get(id);
    }

    // Método transaccional con concurrencia segura
    public boolean transferir(String fromId, String toId, double monto) {
        // Validación básica
        if (fromId.equals(toId) || monto <= 0) {
            return false;
        }

        Cuenta origen = cuentasDB.get(fromId);
        Cuenta destino = cuentasDB.get(toId);

        if (origen == null || destino == null) {
            return false; // Alguna cuenta no existe
        }

        // PREVENCIÓN DE DEADLOCKS:
        // Siempre bloqueamos los objetos en el mismo orden (alfabético por ID)
        // sin importar la dirección de la transferencia.
        Cuenta primerLock = fromId.compareTo(toId) < 0 ? origen : destino;
        Cuenta segundoLock = fromId.compareTo(toId) < 0 ? destino : origen;

        synchronized (primerLock) {
            synchronized (segundoLock) {
                // Ya que tenemos bloqueadas AMBAS cuentas, hacemos el movimiento seguro
                if (origen.getBalance() >= monto) {
                    origen.retirar(monto);
                    destino.depositar(monto);
                    return true;
                }
                return false; // Fondos insuficientes
            }
        }
    }
}