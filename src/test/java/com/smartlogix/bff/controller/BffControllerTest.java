package com.smartlogix.bff.controller;

import com.smartlogix.bff.service.BffService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Pruebas unitarias de la capa de presentacion del BFF.
// Se simula el BffService con Mockito: se prueba solo el mapeo HTTP y la rama 404.
@ExtendWith(MockitoExtension.class)
class BffControllerTest {

    @Mock
    private BffService bffService;

    @InjectMocks
    private BffController bffController;

    @Test
    void obtenerProductos_debeDelegarEnElService() {
        when(bffService.obtenerProductos()).thenReturn(List.of(Map.of("id", 1)));

        List<Map> resultado = bffController.obtenerProductos();

        assertEquals(1, resultado.size());
        verify(bffService).obtenerProductos();
    }

    @Test
    void obtenerProductoPorId_cuandoExiste_debeRetornar200() {
        when(bffService.obtenerProductoPorId(1L)).thenReturn(Map.of("id", 1, "nombre", "Mouse"));

        ResponseEntity<Map> resp = bffController.obtenerProductoPorId(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Mouse", resp.getBody().get("nombre"));
    }

    @Test
    void obtenerProductoPorId_cuandoNoExiste_debeRetornar404() {
        when(bffService.obtenerProductoPorId(99L)).thenReturn(null);

        ResponseEntity<Map> resp = bffController.obtenerProductoPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void obtenerPedidos_debeDelegarEnElService() {
        when(bffService.obtenerPedidos()).thenReturn(List.of(Map.of("id", 10)));

        List<Map> resultado = bffController.obtenerPedidos();

        assertEquals(1, resultado.size());
        verify(bffService).obtenerPedidos();
    }

    @Test
    void obtenerDashboard_debeRetornarResumenAgregado() {
        when(bffService.obtenerResumenDashboard())
                .thenReturn(Map.of("productos", List.of(), "pedidos", List.of()));

        Map<String, Object> resp = bffController.obtenerDashboard();

        assertTrue(resp.containsKey("productos"));
        assertTrue(resp.containsKey("pedidos"));
    }
}
