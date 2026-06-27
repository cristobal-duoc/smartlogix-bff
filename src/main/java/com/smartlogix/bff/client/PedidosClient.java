package com.smartlogix.bff.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

// Cliente HTTP del BFF para comunicarse con ms-pedidos
// Misma estrategia que InventarioClient: Circuit Breaker con fallback
@Component
public class PedidosClient {

    private final RestTemplate restTemplate;

    @Value("${pedidos.url}")
    private String pedidosUrl;

    public PedidosClient() {
        this.restTemplate = new RestTemplate();
    }

    // Constructor de visibilidad de paquete para pruebas: permite inyectar un
    // RestTemplate observable (MockRestServiceServer) sin tocar el comportamiento real.
    PedidosClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Obtiene todos los pedidos desde ms-pedidos
    @CircuitBreaker(name = "pedidos", fallbackMethod = "obtenerPedidosFallback")
    public List<Map> obtenerPedidos() {
        String url = pedidosUrl + "/api/pedidos";
        return restTemplate.getForObject(url, List.class);
    }

    // Fallback: retorna lista vacía si ms-pedidos no está disponible
    public List<Map> obtenerPedidosFallback(Throwable throwable) {
        return Collections.emptyList();
    }

    // Crea un pedido en ms-pedidos (operación de escritura).
    // No lleva Circuit Breaker: si la creación falla, el error debe llegar al usuario,
    // no enmascararse con un fallback silencioso.
    public Map crearPedido(Map<String, String> body) {
        String url = pedidosUrl + "/api/pedidos";
        return restTemplate.postForObject(url, body, Map.class);
    }
}
