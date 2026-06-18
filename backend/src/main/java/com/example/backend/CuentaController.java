package com.example.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CuentaController {

    // Inyectamos nuestro motor bancario
    private final BancoCore bancoCore;

    public CuentaController(BancoCore bancoCore) {
        this.bancoCore = bancoCore;
    }

    // Endpoint para consultar el saldo de una cuenta por su ID
    @GetMapping("/accounts/{id}")
    public ResponseEntity<Cuenta> obtenerCuenta(@PathVariable String id) {

        Cuenta cuenta = bancoCore.obtenerCuenta(id);

        if (cuenta == null) {
            // Si el ID no existe (ej. pidieron la cuenta 1,000,000), devolvemos un error 404
            return ResponseEntity.notFound().build();
        }

        // Si la encuentra, devuelve un 200 OK con el JSON de la cuenta
        return ResponseEntity.ok(cuenta);
    }

    // Endpoint para transferencia entre dos cuentas
    @PostMapping("/transactions/transfer")
    public ResponseEntity<String> realizarTransferencia(@RequestBody TransferenciaRequest request) {

        boolean exito = bancoCore.transferir(
                request.getSourceAccountId(),
                request.getTargetAccountId(),
                request.getAmount()
        );

        if (exito) {
            // Retornamos 200 OK como pide el estándar REST
            return ResponseEntity.ok("Transferencia exitosa");
        } else {
            // Retornamos 400 Bad Request si no hay fondos, cuentas inválidas, etc.
            return ResponseEntity.badRequest().body("Error en la transferencia: fondos insuficientes o cuentas no válidas");
        }
    }
}