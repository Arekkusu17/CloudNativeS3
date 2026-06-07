package cl.duoc.cloudnative.guiasdespacho.dto;

import cl.duoc.cloudnative.guiasdespacho.model.Transportista;

public record TransportistaResponse(
        Long id,
        String nombre,
        String rut,
        String emailContacto
) {
    public static TransportistaResponse from(Transportista transportista) {
        return new TransportistaResponse(
                transportista.getId(),
                transportista.getNombre(),
                transportista.getRut(),
                transportista.getEmailContacto()
        );
    }
}
