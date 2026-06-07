package cl.duoc.cloudnative.guiasdespacho.controller;

import cl.duoc.cloudnative.guiasdespacho.dto.GuiaArchivoResponse;
import cl.duoc.cloudnative.guiasdespacho.dto.GuiaDespachoRequest;
import cl.duoc.cloudnative.guiasdespacho.dto.GuiaDespachoResponse;
import cl.duoc.cloudnative.guiasdespacho.service.GuiaDespachoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guias")
public class GuiaDespachoController {

    private static final MediaType TEXT = MediaType.TEXT_PLAIN;

    private final GuiaDespachoService guiaDespachoService;

    public GuiaDespachoController(GuiaDespachoService guiaDespachoService) {
        this.guiaDespachoService = guiaDespachoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuiaDespachoResponse crearGuia(@RequestBody GuiaDespachoRequest request) {
        return guiaDespachoService.crearGuia(request);
    }

    @PostMapping("/{id}/s3")
    @ResponseStatus(HttpStatus.CREATED)
    public GuiaArchivoResponse subirGuiaGenerada(@PathVariable Long id) {
        return guiaDespachoService.subirGuiaGenerada(id);
    }

    @GetMapping("/{id}/s3")
    public ResponseEntity<byte[]> descargarGuiaDesdeS3(
            @PathVariable Long id,
            @RequestParam Long transportistaId) {
        byte[] contenido = guiaDespachoService.descargarGuiaDesdeS3(id, transportistaId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + guiaDespachoService.nombreArchivo(id) + "\"")
                .contentType(TEXT)
                .body(contenido);
    }

    @PutMapping("/{id}")
    public GuiaDespachoResponse actualizarGuia(
            @PathVariable Long id,
            @RequestBody GuiaDespachoRequest request) {
        return guiaDespachoService.actualizarGuia(id, request);
    }

    @PutMapping("/{id}/s3")
    public GuiaArchivoResponse reemplazarGuiaEnS3(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return guiaDespachoService.reemplazarGuia(id, file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarGuia(@PathVariable Long id) {
        guiaDespachoService.eliminarGuia(id);
    }

    @GetMapping
    public List<GuiaDespachoResponse> consultarGuias(
            @RequestParam Long transportistaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return guiaDespachoService.consultarPorTransportistaYFecha(transportistaId, fecha);
    }
}
