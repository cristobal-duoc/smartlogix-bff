package com.smartlogix.bff.service;

import com.smartlogix.bff.client.InventarioClient;
import com.smartlogix.bff.client.PedidosClient;
import com.smartlogix.bff.client.EnviosClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Capa de servicio del BFF
// Responsabilidad: agregar datos de múltiples microservicios en una sola respuesta
// El BFF NO tiene base de datos, NO tiene entidades JPA — solo consume y combina datos
// @Service: Spring lo registra como componente de lógica de negocio
@Service
public class BffService {

    // Clientes HTTP que se comunican con los microservicios
    private final InventarioClient inventarioClient;
    private final PedidosClient pedidosClient;
    private final EnviosClient enviosClient;

    // Inyección por constructor: las dependencias son explícitas y testeables
    public BffService(InventarioClient inventarioClient, PedidosClient pedidosClient,
                      EnviosClient enviosClient) {
        this.inventarioClient = inventarioClient;
        this.pedidosClient = pedidosClient;
        this.enviosClient = enviosClient;
    }

    // Obtiene todos los productos desde ms-inventario
    // El Circuit Breaker en InventarioClient devuelve lista vacía si el servicio falla
    public List<Map> obtenerProductos() {
        return inventarioClient.obtenerProductos();
    }

    // Obtiene un producto específico por su ID
    // Retorna null si no existe o si ms-inventario no está disponible
    public Map obtenerProductoPorId(Long id) {
        return inventarioClient.obtenerProductoPorId(id);
    }

    // Obtiene todos los pedidos desde ms-pedidos
    public List<Map> obtenerPedidos() {
        return pedidosClient.obtenerPedidos();
    }

    // Obtiene todos los envios desde ms-envios
    // El Circuit Breaker en EnviosClient devuelve lista vacía si el servicio falla
    public List<Map> obtenerEnvios() {
        return enviosClient.obtenerEnvios();
    }

    // Agrega datos de inventario, pedidos y envios en una sola respuesta para el frontend
    // Este es el valor principal del BFF: el frontend hace 1 llamada en vez de 3
    public Map<String, Object> obtenerResumenDashboard() {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("productos", inventarioClient.obtenerProductos());
        resumen.put("pedidos", pedidosClient.obtenerPedidos());
        resumen.put("envios", enviosClient.obtenerEnvios());
        return resumen;
    }

    // --- Operaciones de creación (escritura): el BFF reenvía al microservicio correspondiente ---

    public Map crearProducto(Map<String, Object> body) {
        return inventarioClient.crearProducto(body);
    }

    public Map crearPedido(Map<String, String> body) {
        return pedidosClient.crearPedido(body);
    }

    public Map crearEnvio(Map<String, Object> body) {
        return enviosClient.crearEnvio(body);
    }
}
