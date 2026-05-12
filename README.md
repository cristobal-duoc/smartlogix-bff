# bff

Backend for Frontend (BFF) de SmartLogix.

## Responsabilidad

Intermediario entre el frontend React y los microservicios internos. Agrega datos de múltiples microservicios en una sola respuesta, y protege las llamadas con el patrón Circuit Breaker.

## Puerto

`8080`

## Tecnologías

- Java 17
- Spring Boot 3.2.5
- Resilience4j 2.2.0 (Circuit Breaker)
- Spring AOP (requerido por Resilience4j)

## Patrones de diseño

- **Circuit Breaker**: `@CircuitBreaker` en `InventarioClient` y `PedidosClient`. Si un microservicio falla, el circuito se abre y se retorna un fallback (lista vacía) en lugar de propagar el error al frontend.

## Arquitectura

```
Frontend → [BFF] → ms-inventario (puerto 8081)
                 → ms-pedidos    (puerto 8082)
```

El BFF **no tiene base de datos**. Su arquitectura es:

```
Controller → Service → Client (HTTP)
```

No tiene Repository ni Entity porque no persiste datos propios.

## Estados del Circuit Breaker

| Estado | Descripción |
|--------|-------------|
| CERRADO | Funcionamiento normal, llamadas fluyen al microservicio |
| ABIERTO | Tasa de fallos supera 50%, se retorna fallback directamente |
| SEMI-ABIERTO | Permite 3 llamadas de prueba para verificar recuperación |

## Ejecutar

```bash
mvn spring-boot:run
```

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/bff/productos` | Productos desde ms-inventario |
| GET | `/api/bff/productos/{id}` | Producto por ID |
| GET | `/api/bff/pedidos` | Pedidos desde ms-pedidos |
| GET | `/api/bff/dashboard` | Productos + pedidos agregados |
| GET | `/actuator/health` | Estado del Circuit Breaker |

## Tests

```bash
mvn test
```

- `BffServiceTest`: prueba la lógica de agregación con mocks de los clientes (6 casos)
