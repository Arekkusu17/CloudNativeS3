package cl.duoc.cloudnative.guiasdespacho.service;

import cl.duoc.cloudnative.guiasdespacho.dto.TransportistaRequest;
import cl.duoc.cloudnative.guiasdespacho.dto.TransportistaResponse;
import cl.duoc.cloudnative.guiasdespacho.model.Transportista;
import cl.duoc.cloudnative.guiasdespacho.repository.TransportistaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransportistaService {

    private final TransportistaRepository transportistaRepository;

    public TransportistaService(TransportistaRepository transportistaRepository) {
        this.transportistaRepository = transportistaRepository;
    }

    public List<TransportistaResponse> listarTransportistas() {
        return transportistaRepository.findAll()
                .stream()
                .map(TransportistaResponse::from)
                .toList();
    }

    public TransportistaResponse crearTransportista(TransportistaRequest request) {
        validar(request);
        String rut = request.rut().trim();
        if (transportistaRepository.existsByRut(rut)) {
            throw new IllegalArgumentException("Ya existe un transportista con el RUT indicado.");
        }
        Transportista transportista = new Transportista(
                request.nombre().trim(),
                rut,
                request.emailContacto().trim()
        );
        return TransportistaResponse.from(transportistaRepository.save(transportista));
    }

    private void validar(TransportistaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El transportista es obligatorio.");
        }
        if (esTextoVacio(request.nombre())) {
            throw new IllegalArgumentException("El nombre del transportista es obligatorio.");
        }
        if (esTextoVacio(request.rut())) {
            throw new IllegalArgumentException("El RUT del transportista es obligatorio.");
        }
        if (esTextoVacio(request.emailContacto())) {
            throw new IllegalArgumentException("El email de contacto es obligatorio.");
        }
    }

    private boolean esTextoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
