package cl.duoc.cloudnative.guiasdespacho.dto;

import cl.duoc.cloudnative.guiasdespacho.model.GuiaDespacho;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GuiaDespachoResponse(
        Long id,
        String numeroGuia,
        String numeroPedido,
        String clienteNombre,
        String direccionDestino,
        String comunaDestino,
        String descripcionCarga,
        BigDecimal pesoKg,
        Long transportistaId,
        String transportistaNombre,
        String nombreArchivo,
        String rutaEfs,
        String bucketS3,
        String keyS3,
        LocalDateTime fechaEmision,
        LocalDateTime fechaSubidaS3,
        String estado
) {
    public static GuiaDespachoResponse from(GuiaDespacho guia) {
        return new GuiaDespachoResponse(
                guia.getId(),
                guia.getNumeroGuia(),
                guia.getPedido().getNumeroPedido(),
                guia.getPedido().getClienteNombre(),
                guia.getPedido().getDireccionDestino(),
                guia.getPedido().getComunaDestino(),
                guia.getPedido().getDescripcionCarga(),
                guia.getPedido().getPesoKg(),
                guia.getTransportista().getId(),
                guia.getTransportista().getNombre(),
                guia.getNombreArchivo(),
                guia.getRutaEfs(),
                guia.getBucketS3(),
                guia.getKeyS3(),
                guia.getFechaEmision(),
                guia.getFechaSubidaS3(),
                guia.getEstado()
        );
    }
}
