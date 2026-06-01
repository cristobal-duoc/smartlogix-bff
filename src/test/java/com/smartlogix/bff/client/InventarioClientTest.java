package com.smartlogix.bff.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// Pruebas unitarias del cliente HTTP hacia ms-inventario.
// Se usa MockRestServiceServer para simular las respuestas del microservicio,
// y se prueban tanto las llamadas exitosas como los metodos de fallback del Circuit Breaker.
class InventarioClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private InventarioClient inventarioClient;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        inventarioClient = new InventarioClient(restTemplate);
        ReflectionTestUtils.setField(inventarioClient, "inventarioUrl", "http://ms-inventario");
    }

    @Test
    void obtenerProductos_debePedirLaListaYDeserializarla() {
        server.expect(requestTo("http://ms-inventario/inventario/productos"))
              .andRespond(withSuccess("[{\"id\":1,\"nombre\":\"Mouse\"}]", MediaType.APPLICATION_JSON));

        List<Map> productos = inventarioClient.obtenerProductos();

        assertEquals(1, productos.size());
        server.verify();
    }

    @Test
    void obtenerProductoPorId_debePedirElProductoConcreto() {
        server.expect(requestTo("http://ms-inventario/inventario/productos/5"))
              .andRespond(withSuccess("{\"id\":5,\"nombre\":\"Teclado\"}", MediaType.APPLICATION_JSON));

        Map producto = inventarioClient.obtenerProductoPorId(5L);

        assertEquals("Teclado", producto.get("nombre"));
        server.verify();
    }

    @Test
    void obtenerProductosFallback_debeRetornarListaVacia() {
        List<Map> resultado = inventarioClient.obtenerProductosFallback(new RuntimeException("caido"));

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtenerProductoPorIdFallback_debeRetornarNull() {
        Map resultado = inventarioClient.obtenerProductoPorIdFallback(1L, new RuntimeException("caido"));

        assertNull(resultado);
    }
}
