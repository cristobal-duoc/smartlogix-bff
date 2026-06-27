package com.smartlogix.bff.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

// Cliente HTTP del BFF para comunicarse con ms-envios.
// Misma estrategia que InventarioClient y PedidosClient: Circuit Breaker con fallback,
// para que la caida del microservicio de envios no propague el error al usuario.
@Component
public class EnviosClient {

    private final RestTemplate restTemplate;

    // URL base de ms-envios, leida desde application.properties
    @Value("${envios.url}")
    private String enviosUrl;

    public EnviosClient() {
        this.restTemplate = new RestTemplate();
    }

    // Constructor de visibilidad de paquete para pruebas: permite inyectar un
    // RestTemplate observable (MockRestServiceServer) sin tocar el comportamiento real.
    EnviosClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Obtiene todos los envios desde ms-envios.
    // @CircuitBreaker: si ms-envios falla, Resilience4j llama a obtenerEnviosFallback().
    @CircuitBreaker(name = "envios", fallbackMethod = "obtenerEnviosFallback")
    public List<Map> obtenerEnvios() {
        String url = enviosUrl + "/envios";
        return restTemplate.getForObject(url, List.class);
    }

    // Fallback: retorna lista vacia si ms-envios no esta disponible.
    public List<Map> obtenerEnviosFallback(Throwable throwable) {
        return Collections.emptyList();
    }

    // Crea un envio en ms-envios (operación de escritura, sin Circuit Breaker).
    public Map crearEnvio(Map<String, Object> body) {
        String url = enviosUrl + "/envios";
        return restTemplate.postForObject(url, body, Map.class);
    }
}
