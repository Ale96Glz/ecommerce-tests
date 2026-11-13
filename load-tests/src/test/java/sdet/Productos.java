package sdet;

import static io.gatling.javaapi.core.CoreDsl.jsonPath;

import io.gatling.javaapi.core.CheckBuilder;

/** Selección de producto para las simulaciones: nunca el primero del listado, que puede estar agotado o caro. */
final class Productos {
  private static final double TOPE_PAGO = 100.0;

  private Productos() {}

  static CheckBuilder.Final conStock() {
    return jsonPath("$[?(@.estado == 'ACTIVO' && @.stock > 0)].id")
        .ofLong()
        .find(0)
        .saveAs("productoId");
  }

  static CheckBuilder.Final pagable() {
    return jsonPath("$[?(@.estado == 'ACTIVO' && @.stock > 0 && @.precio <= " + TOPE_PAGO + ")].id")
        .ofLong()
        .find(0)
        .saveAs("productoId");
  }
}
