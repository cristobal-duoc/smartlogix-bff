package com.smartlogix.bff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// Genera y exporta la especificacion OpenAPI (Swagger) del BFF.
// Levanta el contexto (sin necesidad de los microservicios downstream), consulta
// /v3/api-docs (springdoc) y guarda el JSON en api-docs/openapi.json.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiExportTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void exportarEspecificacionOpenApi() throws Exception {
        String json = restTemplate.getForObject(
                "http://localhost:" + port + "/v3/api-docs", String.class);

        assertNotNull(json);
        assertTrue(json.contains("/api/bff"),
                "La especificacion debe incluir los endpoints del BFF");

        Path dir = Path.of("api-docs");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("openapi.json"), json);
    }
}
