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

// Pruebas unitarias del cliente HTTP hacia ms-pedidos.
// MockRestServiceServer simula las respuestas; se cubre la llamada exitosa y el fallback.
class PedidosClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private PedidosClient pedidosClient;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        pedidosClient = new PedidosClient(restTemplate);
        ReflectionTestUtils.setField(pedidosClient, "pedidosUrl", "http://ms-pedidos");
    }

    @Test
    void obtenerPedidos_debePedirLaListaYDeserializarla() {
        server.expect(requestTo("http://ms-pedidos/api/pedidos"))
              .andRespond(withSuccess("[{\"id\":1,\"codigo\":\"PED-001\"}]", MediaType.APPLICATION_JSON));

        List<Map> pedidos = pedidosClient.obtenerPedidos();

        assertEquals(1, pedidos.size());
        server.verify();
    }

    @Test
    void obtenerPedidosFallback_debeRetornarListaVacia() {
        List<Map> resultado = pedidosClient.obtenerPedidosFallback(new RuntimeException("caido"));

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
