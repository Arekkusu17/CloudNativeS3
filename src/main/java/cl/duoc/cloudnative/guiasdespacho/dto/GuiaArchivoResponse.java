package cl.duoc.cloudnative.guiasdespacho.dto;

public record GuiaArchivoResponse(
        Long guiaId,
        String bucket,
        String key,
        String nombreArchivo,
        String rutaEfs
) {
}
