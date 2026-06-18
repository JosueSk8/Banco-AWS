package com.example.backend;

public class TransferenciaRequest {
    private String sourceAccountId;
    private String targetAccountId;
    private double amount;

    // Getters
    public String getSourceAccountId() { return sourceAccountId; }
    public String getTargetAccountId() { return targetAccountId; }
    public double getAmount() { return amount; }

    // Setters (Necesarios para que Spring Boot pueda armar el objeto desde el JSON)
    public void setSourceAccountId(String sourceAccountId) { this.sourceAccountId = sourceAccountId; }
    public void setTargetAccountId(String targetAccountId) { this.targetAccountId = targetAccountId; }
    public void setAmount(double amount) { this.amount = amount; }
}