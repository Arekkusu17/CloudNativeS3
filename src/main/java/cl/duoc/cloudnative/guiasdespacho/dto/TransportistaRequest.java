package cl.duoc.cloudnative.guiasdespacho.dto;

public record TransportistaRequest(
        String nombre,
        String rut,
        String emailContacto
) {
}
