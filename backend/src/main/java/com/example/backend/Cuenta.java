package com.example.backend;

public class Cuenta {
    private final String id;
    private final String propietario;
    private double balance;

    public Cuenta(String id, String propietario, double balance) {
        this.id = id;
        this.propietario = propietario;
        this.balance = balance;
    }

    // Getters
    public String getId() { return id; }
    public String getPropietario() { return propietario; }
    public synchronized double getBalance() { return balance; }

    // Métodos para modificar el saldo de forma segura
    public synchronized void depositar(double monto) {
        this.balance += monto;
    }

    public synchronized boolean retirar(double monto) {
        if (this.balance >= monto) {
            this.balance -= monto;
            return true;
        }
        return false; // Fondos insuficientes
    }
}