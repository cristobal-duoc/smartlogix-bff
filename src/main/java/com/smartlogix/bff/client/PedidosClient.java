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
}
