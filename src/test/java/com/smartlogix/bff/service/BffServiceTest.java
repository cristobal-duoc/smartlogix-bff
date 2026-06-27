package com.smartlogix.bff.service;

import com.smartlogix.bff.client.InventarioClient;
import com.smartlogix.bff.client.PedidosClient;
import com.smartlogix.bff.client.EnviosClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Pruebas unitarias del BffService
// No levanta Spring ni hace llamadas HTTP reales — usa mocks de los clientes
@ExtendWith(MockitoExtension.class)
class BffServiceTest {

    @Mock
    private InventarioClient inventarioClient;

    @Mock
    private PedidosClient pedidosClient;

    @Mock
    private EnviosClient enviosClient;

    @InjectMocks
    private BffService bffService;

    @Test
    void obtenerProductos_debeRetornarListaDeProductos() {
        // Arrange: el cliente de inventario retorna 2 productos simulados
        List<Map> productosSimulados = Arrays.asList(
                Map.of("id", 1, "nombre", "Camiseta M", "stock", 50),
                Map.of("id", 2, "nombre", "Camiseta L", "stock", 30)
        );
        when(inventarioClient.obtenerProductos()).thenReturn(productosSimulados);

        // Act
        List<Map> resultado = bffService.obtenerProductos();

        // Assert: se retornaron 2 productos
        assertEquals(2, resultado.size());
        verify(inventarioClient, times(1)).obtenerProductos();
    }

    @Test
    void obtenerProductos_cuandoMsInventarioFalla_debeRetornarListaVacia() {
        // Arrange: simula el comportamiento del fallback del Circuit Breaker
        when(inventarioClient.obtenerProductos()).thenReturn(Collections.emptyList());

        // Act
        List<Map> resultado = bffService.obtenerProductos();

        // Assert: la lista está vacía (fallback del circuit breaker)
        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtenerPedidos_debeRetornarListaDePedidos() {
        // Arrange
        List<Map> pedidosSimulados = Arrays.asList(
                Map.of("id", 1, "codigo", "ORD-001", "tipo", "NORMAL", "estado", "PENDIENTE")
        );
        when(pedidosClient.obtenerPedidos()).thenReturn(pedidosSimulados);

        // Act
        List<Map> resultado = bffService.obtenerPedidos();

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("ORD-001", resultado.get(0).get("codigo"));
        verify(pedidosClient, times(1)).obtenerPedidos();
    }

    @Test
    void obtenerEnvios_debeRetornarListaDeEnvios() {
        // Arrange: el cliente de envios retorna un envio simulado
        List<Map> enviosSimulados = Arrays.asList(
                Map.of("id", 1, "pedidoId", 1, "transportista", "Chilexpress", "estado", "EN_RUTA")
        );
        when(enviosClient.obtenerEnvios()).thenReturn(enviosSimulados);

        // Act
        List<Map> resultado = bffService.obtenerEnvios();

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Chilexpress", resultado.get(0).get("transportista"));
        verify(enviosClient, times(1)).obtenerEnvios();
    }

    @Test
    void obtenerResumenDashboard_debeAgregarProductosPedidosYEnvios() {
        // Arrange: cada cliente retorna sus datos
        List<Map> productos = Arrays.asList(Map.of("id", 1, "nombre", "Camiseta M"));
        List<Map> pedidos = Arrays.asList(Map.of("id", 1, "codigo", "ORD-001"));
        List<Map> envios = Arrays.asList(Map.of("id", 1, "transportista", "Chilexpress"));

        when(inventarioClient.obtenerProductos()).thenReturn(productos);
        when(pedidosClient.obtenerPedidos()).thenReturn(pedidos);
        when(enviosClient.obtenerEnvios()).thenReturn(envios);

        // Act: el BFF agrega las tres listas en un solo Map
        Map<String, Object> dashboard = bffService.obtenerResumenDashboard();

        // Assert: el dashboard contiene productos, pedidos y envios
        assertNotNull(dashboard);
        assertTrue(dashboard.containsKey("productos"));
        assertTrue(dashboard.containsKey("pedidos"));
        assertTrue(dashboard.containsKey("envios"));

        // Verify: cada cliente fue consultado exactamente una vez
        verify(inventarioClient, times(1)).obtenerProductos();
        verify(pedidosClient, times(1)).obtenerPedidos();
        verify(enviosClient, times(1)).obtenerEnvios();
    }

    @Test
    void obtenerProductoPorId_cuandoExiste_debeRetornarProducto() {
        // Arrange
        Map productoSimulado = Map.of("id", 1L, "nombre", "Camiseta M", "stock", 50);
        when(inventarioClient.obtenerProductoPorId(1L)).thenReturn(productoSimulado);

        // Act
        Map resultado = bffService.obtenerProductoPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Camiseta M", resultado.get("nombre"));
    }

    @Test
    void obtenerProductoPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange: fallback del Circuit Breaker retorna null
        when(inventarioClient.obtenerProductoPorId(99L)).thenReturn(null);

        // Act
        Map resultado = bffService.obtenerProductoPorId(99L);

        // Assert: null indica que el producto no existe o el servicio no está disponible
        assertNull(resultado);
    }
}
