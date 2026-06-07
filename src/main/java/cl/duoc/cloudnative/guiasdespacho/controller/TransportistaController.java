package cl.duoc.cloudnative.guiasdespacho.controller;

import cl.duoc.cloudnative.guiasdespacho.dto.TransportistaRequest;
import cl.duoc.cloudnative.guiasdespacho.dto.TransportistaResponse;
import cl.duoc.cloudnative.guiasdespacho.service.TransportistaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transportistas")
public class TransportistaController {

    private final TransportistaService transportistaService;

    public TransportistaController(TransportistaService transportistaService) {
        this.transportistaService = transportistaService;
    }

    @GetMapping
    public List<TransportistaResponse> listarTransportistas() {
        return transportistaService.listarTransportistas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransportistaResponse crearTransportista(@RequestBody TransportistaRequest request) {
        return transportistaService.crearTransportista(request);
    }
}
