package com.smartlogix.bff.controller;

import com.smartlogix.bff.service.BffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Capa de presentación del BFF
// Expone los endpoints que el frontend React consume
// Solo tiene Controller → Service → Client (sin Repository ni Entity)
// @RestController: todas las respuestas se serializan automáticamente a JSON
// @RequestMapping: todos los endpoints empiezan con /api/bff
@RestController
@RequestMapping("/api/bff")
public class BffController {

    private final BffService bffService;

    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    // GET /api/bff/productos → lista todos los productos (proxy a ms-inventario)
    @GetMapping("/productos")
    public List<Map> obtenerProductos() {
        return bffService.obtenerProductos();
    }

    // GET /api/bff/productos/{id} → producto específico
    // Retorna 404 si no existe o si ms-inventario no está disponible
    @GetMapping("/productos/{id}")
    public ResponseEntity<Map> obtenerProductoPorId(@PathVariable Long id) {
        Map producto = bffService.obtenerProductoPorId(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    // GET /api/bff/pedidos → lista todos los pedidos (proxy a ms-pedidos)
    @GetMapping("/pedidos")
    public List<Map> obtenerPedidos() {
        return bffService.obtenerPedidos();
    }

    // GET /api/bff/envios → lista todos los envíos (proxy a ms-envios)
    @GetMapping("/envios")
    public List<Map> obtenerEnvios() {
        return bffService.obtenerEnvios();
    }

    // GET /api/bff/dashboard → datos agregados: productos + pedidos + envíos en una sola llamada
    // Permite al frontend cargar el dashboard con un solo request HTTP
    @GetMapping("/dashboard")
    public Map<String, Object> obtenerDashboard() {
        return bffService.obtenerResumenDashboard();
    }

    // --- Creación: el frontend crea recursos a través del BFF (POST) ---

    // POST /api/bff/productos → crea un producto en ms-inventario
    @PostMapping("/productos")
    public ResponseEntity<Map> crearProducto(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(bffService.crearProducto(body));
    }

    // POST /api/bff/pedidos → crea un pedido en ms-pedidos
    @PostMapping("/pedidos")
    public ResponseEntity<Map> crearPedido(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(bffService.crearPedido(body));
    }

    // POST /api/bff/envios → crea un envío en ms-envios
    @PostMapping("/envios")
    public ResponseEntity<Map> crearEnvio(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(bffService.crearEnvio(body));
    }
}
