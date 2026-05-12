package com.smartlogix.bff.service;

import com.smartlogix.bff.client.InventarioClient;
import com.smartlogix.bff.client.PedidosClient;
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

    // Inyección por constructor: las dependencias son explícitas y testeables
    public BffService(InventarioClient inventarioClient, PedidosClient pedidosClient) {
        this.inventarioClient = inventarioClient;
        this.pedidosClient = pedidosClient;
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

    // Agrega datos de inventario y pedidos en una sola respuesta para el frontend
    // Este es el valor principal del BFF: el frontend hace 1 llamada en vez de 2
    public Map<String, Object> obtenerResumenDashboard() {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("productos", inventarioClient.obtenerProductos());
        resumen.put("pedidos", pedidosClient.obtenerPedidos());
        return resumen;
    }
}
