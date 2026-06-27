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

// Pruebas unitarias del cliente HTTP hacia ms-envios.
// MockRestServiceServer simula las respuestas; se cubre la llamada exitosa y el fallback.
class EnviosClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private EnviosClient enviosClient;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        enviosClient = new EnviosClient(restTemplate);
        ReflectionTestUtils.setField(enviosClient, "enviosUrl", "http://ms-envios");
    }

    @Test
    void obtenerEnvios_debePedirLaListaYDeserializarla() {
        server.expect(requestTo("http://ms-envios/envios"))
              .andRespond(withSuccess(
                      "[{\"id\":1,\"transportista\":\"Chilexpress\",\"estado\":\"EN_RUTA\"}]",
                      MediaType.APPLICATION_JSON));

        List<Map> envios = enviosClient.obtenerEnvios();

        assertEquals(1, envios.size());
        assertEquals("Chilexpress", envios.get(0).get("transportista"));
        server.verify();
    }

    @Test
    void obtenerEnviosFallback_debeRetornarListaVacia() {
        List<Map> resultado = enviosClient.obtenerEnviosFallback(new RuntimeException("caido"));

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void crearEnvio_debeEnviarPostYRetornarElCreado() {
        server.expect(requestTo("http://ms-envios/envios"))
              .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.method(org.springframework.http.HttpMethod.POST))
              .andRespond(withSuccess("{\"id\":7,\"transportista\":\"Starken\",\"estado\":\"PREPARANDO\"}", MediaType.APPLICATION_JSON));

        Map creado = enviosClient.crearEnvio(java.util.Map.of("pedidoId", 1, "transportista", "Starken", "estado", "PREPARANDO"));

        assertEquals("Starken", creado.get("transportista"));
        server.verify();
    }
}
