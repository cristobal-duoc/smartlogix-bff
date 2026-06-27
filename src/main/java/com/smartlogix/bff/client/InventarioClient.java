package com.smartlogix.bff.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

// Cliente HTTP del BFF para comunicarse con ms-inventario
// Implementa el patrón Circuit Breaker usando Resilience4j:
//   CERRADO → llamadas normales al microservicio
//   ABIERTO  → si hay muchos fallos, el circuito se abre y se retorna el fallback
//   SEMI-ABIERTO → el circuito deja pasar algunas llamadas de prueba para verificar recuperación
// @Component: Spring lo registra como bean para que pueda ser inyectado en BffService
@Component
public class InventarioClient {

    // RestTemplate: cliente HTTP de Spring para hacer llamadas REST síncronas
    private final RestTemplate restTemplate;

    // URL base de ms-inventario, leída desde application.properties
    @Value("${inventario.url}")
    private String inventarioUrl;

    public InventarioClient() {
        // Se instancia directamente porque RestTemplate no necesita configuración especial
        this.restTemplate = new RestTemplate();
    }

    // Constructor de visibilidad de paquete para pruebas: permite inyectar un
    // RestTemplate observable (MockRestServiceServer) sin tocar el comportamiento real.
    InventarioClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Obtiene todos los productos del microservicio de inventario
    // @CircuitBreaker: si ms-inventario falla, Resilience4j llama a obtenerProductosFallback()
    // name = "inventario": corresponde a la configuración en application.properties
    @CircuitBreaker(name = "inventario", fallbackMethod = "obtenerProductosFallback")
    public List<Map> obtenerProductos() {
        String url = inventarioUrl + "/inventario/productos";
        // getForObject: hace GET y deserializa la respuesta JSON a una lista
        return restTemplate.getForObject(url, List.class);
    }

    // Fallback del Circuit Breaker: se ejecuta cuando ms-inventario no está disponible
    // El parámetro Throwable recibe la excepción que causó el fallo
    // Retorna una lista vacía para que el frontend no muestre error al usuario
    public List<Map> obtenerProductosFallback(Throwable throwable) {
        // En producción esto se podría loggear: log.warn("ms-inventario no disponible", throwable)
        return Collections.emptyList();
    }

    // Obtiene un producto específico por su ID
    // Si ms-inventario falla, el fallback retorna null y el servicio devuelve 503
    @CircuitBreaker(name = "inventario", fallbackMethod = "obtenerProductoPorIdFallback")
    public Map obtenerProductoPorId(Long id) {
        String url = inventarioUrl + "/inventario/productos/" + id;
        return restTemplate.getForObject(url, Map.class);
    }

    // Fallback para obtenerProductoPorId: retorna null indicando que el servicio no está disponible
    public Map obtenerProductoPorIdFallback(Long id, Throwable throwable) {
        return null;
    }

    // Crea un producto en ms-inventario (operación de escritura, sin Circuit Breaker).
    public Map crearProducto(Map<String, Object> body) {
        String url = inventarioUrl + "/inventario/productos";
        return restTemplate.postForObject(url, body, Map.class);
    }
}
