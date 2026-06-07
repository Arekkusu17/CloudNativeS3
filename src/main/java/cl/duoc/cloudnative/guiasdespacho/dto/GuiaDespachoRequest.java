package cl.duoc.cloudnative.guiasdespacho.dto;

import java.math.BigDecimal;

public record GuiaDespachoRequest(
        String numeroPedido,
        String clienteNombre,
        String direccionDestino,
        String comunaDestino,
        String descripcionCarga,
        BigDecimal pesoKg,
        Long transportistaId
) {
}
