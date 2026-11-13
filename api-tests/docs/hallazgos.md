# Hallazgos de contrato

Defectos de negocio vistos desde el gateway. El caso en `casos.md` describe **lo que el API hace hoy**. Aquí va **lo que debería hacer** y cómo comprobarlo cuando se corrija el backend.

---

## H-PAGO-01 — El monto del pago no se valida contra el total del pedido

| Campo | Valor |
|---|---|
| Fecha | 10 de noviembre de 2026 |
| Cerrado | 11 de noviembre de 2026 |
| Severidad | Alta (dinero) |
| Servicio | Pagos, vía `POST /api/v1/pago` |
| Ambiente | Gateway `https://ecommerce.local` |
| Estado | Cerrado. Opción A: 409 y el pedido sigue `CREADO` |

### Qué ocurría

`POST /api/v1/pago` trataba `monto` como un número independiente. Si era mayor que 0 y no superaba el tope de pasarela (100.00), el pago quedaba `PROCESADO` aunque no coincidiera con el total del pedido.

Evidencia previa (10 nov 2026): pedido `282`, total **30**, `monto` **31.0** → 201 `PROCESADO` (pago `269`).

### Contrato tras el parche

Corrida de `PagosTest#unMontoDistintoAlDelPedidoRecibe409` (11 nov 2026, 19:15 GMT-6).

1. `POST /api/v1/pedido` — pedido `285`, total **30**.
2. `POST /api/v1/pago` con `pedidoId` 285 y `monto` **31.0**.
3. Respuesta **409**:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "El monto no coincide con el total del pedido"
}
```

El pedido permanece `CREADO`. El caso vive en `PagosTest` y `docs/casos.md`.
