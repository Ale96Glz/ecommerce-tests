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
| Estado | Cerrado. Opción A: 422 y el pedido sigue `CREADO` |

### Qué ocurría

`POST /api/v1/pago` trataba `monto` como un número independiente. Si era mayor que 0 y no superaba el tope de pasarela (100.00), el pago quedaba `PROCESADO` aunque no coincidiera con el total del pedido.

Evidencia previa (10 nov 2026): pedido `282`, total **30**, `monto` **31.0** → 201 `PROCESADO` (pago `269`).

### Contrato tras el parche

Corrida de `PagosTest#unMontoDistintoAlDelPedidoRecibe422` (16 nov 2026).

1. `POST /api/v1/pedido` — pedido `285`, total **30**.
2. `POST /api/v1/pago` con `pedidoId` 285 y `monto` **31.0**.
3. Respuesta **422**:

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "El monto 31.0 no coincide con el total del pedido 30.00"
}
```

El pedido permanece `CREADO`. Se distingue del 409 de estado (pedido ya `PAGADO`): el `422` es la regla de negocio del contenido (ADR-0020 del backend). El caso vive en `PagosTest` y `docs/casos.md`.
