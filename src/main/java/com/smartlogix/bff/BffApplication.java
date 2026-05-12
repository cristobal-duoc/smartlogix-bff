package com.smartlogix.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del BFF (Backend for Frontend)
// El BFF no tiene base de datos propia — solo agrega y transforma datos de los microservicios
@SpringBootApplication
public class BffApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }
}
