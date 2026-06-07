package cl.duoc.cloudnative.guiasdespacho.service;

import cl.duoc.cloudnative.guiasdespacho.dto.GuiaArchivoResponse;
import cl.duoc.cloudnative.guiasdespacho.dto.GuiaDespachoRequest;
import cl.duoc.cloudnative.guiasdespacho.dto.GuiaDespachoResponse;
import cl.duoc.cloudnative.guiasdespacho.model.GuiaDespacho;
import cl.duoc.cloudnative.guiasdespacho.model.Pedido;
import cl.duoc.cloudnative.guiasdespacho.model.Transportista;
import cl.duoc.cloudnative.guiasdespacho.repository.GuiaDespachoRepository;
import cl.duoc.cloudnative.guiasdespacho.repository.PedidoRepository;
import cl.duoc.cloudnative.guiasdespacho.repository.TransportistaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class GuiaDespachoService {

    private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";
    private static final DateTimeFormatter S3_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PedidoRepository pedidoRepository;
    private final TransportistaRepository transportistaRepository;
    private final GuiaDespachoRepository guiaDespachoRepository;
    private final GuiaTextoGenerator guiaTextoGenerator;
    private final S3Client s3Client;
    private final String guiasBucket;
    private final Path guiasEfsDir;

    public GuiaDespachoService(
            PedidoRepository pedidoRepository,
            TransportistaRepository transportistaRepository,
            GuiaDespachoRepository guiaDespachoRepository,
            GuiaTextoGenerator guiaTextoGenerator,
            S3Client s3Client,
            @Value("${app.aws.s3.guias-bucket}") String guiasBucket,
            @Value("${app.guias.efs-dir}") String guiasEfsDir) {
        this.pedidoRepository = pedidoRepository;
        this.transportistaRepository = transportistaRepository;
        this.guiaDespachoRepository = guiaDespachoRepository;
        this.guiaTextoGenerator = guiaTextoGenerator;
        this.s3Client = s3Client;
        this.guiasBucket = guiasBucket;
        this.guiasEfsDir = Paths.get(guiasEfsDir);
    }

    @Transactional
    public GuiaDespachoResponse crearGuia(GuiaDespachoRequest request) {
        validarGuia(request);
        if (pedidoRepository.existsByNumeroPedido(request.numeroPedido().trim())) {
            throw new IllegalArgumentException("Ya existe un pedido con el numero indicado.");
        }

        Transportista transportista = obtenerTransportista(request.transportistaId());
        Pedido pedido = pedidoRepository.save(new Pedido(
                request.numeroPedido().trim(),
                request.clienteNombre().trim(),
                request.direccionDestino().trim(),
                request.comunaDestino().trim(),
                request.descripcionCarga().trim(),
                request.pesoKg()
        ));

        GuiaDespacho guia = new GuiaDespacho(
                pedido,
                transportista,
                "GD-" + pedido.getId(),
                nombreArchivo(pedido.getId())
        );
        GuiaDespacho guiaGuardada = guiaDespachoRepository.save(guia);
        guardarGuiaEnEfs(guiaGuardada);
        return GuiaDespachoResponse.from(guiaGuardada);
    }

    @Transactional
    public GuiaDespachoResponse actualizarGuia(Long id, GuiaDespachoRequest request) {
        validarGuia(request);
        GuiaDespacho guia = obtenerGuia(id);
        Transportista transportista = obtenerTransportista(request.transportistaId());
        guia.getPedido().actualizarDatos(
                request.clienteNombre().trim(),
                request.direccionDestino().trim(),
                request.comunaDestino().trim(),
                request.descripcionCarga().trim(),
                request.pesoKg()
        );
        guia.actualizarTransportista(transportista);
        guardarGuiaEnEfs(guia);
        guia.limpiarS3();
        return GuiaDespachoResponse.from(guia);
    }

    @Transactional
    public GuiaArchivoResponse subirGuiaGenerada(Long id) {
        GuiaDespacho guia = obtenerGuia(id);
        byte[] contenido = guiaTextoGenerator.generarTxt(guia);
        String bucket = obtenerBucket();
        String key = keyGuia(guia);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(CONTENT_TYPE)
                .contentLength((long) contenido.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(contenido));
        String rutaEfs = guardarGuiaEnEfs(guia);
        guia.actualizarS3(bucket, key);
        return respuesta(guia, bucket, key, rutaEfs);
    }

    @Transactional
    public GuiaArchivoResponse reemplazarGuia(Long id, MultipartFile file) {
        GuiaDespacho guia = obtenerGuia(id);
        validarArchivo(file);
        String bucket = obtenerBucket();
        String key = keyGuia(guia);

        try {
            byte[] contenido = file.getBytes();
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType() != null ? file.getContentType() : CONTENT_TYPE)
                            .contentLength((long) contenido.length)
                            .build(),
                    RequestBody.fromBytes(contenido)
            );

            Path ruta = rutaGuiaEfs(guia);
            Files.createDirectories(ruta.getParent());
            Files.write(ruta, contenido);
            guia.actualizarArchivoEfs(nombreArchivo(guia.getId()), ruta.normalize().toString());
            guia.actualizarS3(bucket, key);
            return respuesta(guia, bucket, key, guia.getRutaEfs());
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible leer el archivo enviado.");
        }
    }

    @Transactional(readOnly = true)
    public byte[] descargarGuiaDesdeS3(Long id, Long transportistaId) {
        GuiaDespacho guia = obtenerGuia(id);
        validarPermisoTransportista(guia, transportistaId);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(obtenerBucket())
                .key(obtenerKeyS3(guia))
                .build();

        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
        return response.asByteArray();
    }

    @Transactional
    public void eliminarGuia(Long id) {
        GuiaDespacho guia = obtenerGuia(id);
        if (guia.getKeyS3() != null && !guia.getKeyS3().isBlank()) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(obtenerBucket())
                    .key(guia.getKeyS3())
                    .build());
        }
        guiaDespachoRepository.delete(guia);
    }

    @Transactional(readOnly = true)
    public List<GuiaDespachoResponse> consultarPorTransportistaYFecha(Long transportistaId, LocalDate fecha) {
        if (transportistaId == null || transportistaId <= 0) {
            throw new IllegalArgumentException("El transportista debe ser valido.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de consulta es obligatoria.");
        }
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay().minusNanos(1);
        return guiaDespachoRepository.findByTransportistaIdAndFechaEmisionBetween(transportistaId, inicio, fin)
                .stream()
                .map(GuiaDespachoResponse::from)
                .toList();
    }

    public String nombreArchivo(Long guiaId) {
        return "guia-" + guiaId + ".txt";
    }

    private String guardarGuiaEnEfs(GuiaDespacho guia) {
        Path ruta = rutaGuiaEfs(guia);
        try {
            Files.createDirectories(ruta.getParent());
            Files.write(ruta, guiaTextoGenerator.generarTxt(guia));
            String rutaNormalizada = ruta.normalize().toString();
            guia.actualizarArchivoEfs(nombreArchivo(guia.getId()), rutaNormalizada);
            return rutaNormalizada;
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible guardar la guia temporalmente en EFS.");
        }
    }

    private Path rutaGuiaEfs(GuiaDespacho guia) {
        return guiasEfsDir
                .resolve(LocalDate.now().format(S3_DATE_FORMAT))
                .resolve(slug(guia.getTransportista().getNombre()))
                .resolve(nombreArchivo(guia.getId()));
    }

    private String keyGuia(GuiaDespacho guia) {
        return guia.getFechaEmision().toLocalDate().format(S3_DATE_FORMAT)
                + "/"
                + slug(guia.getTransportista().getNombre())
                + "/"
                + nombreArchivo(guia.getId());
    }

    private String obtenerKeyS3(GuiaDespacho guia) {
        if (guia.getKeyS3() == null || guia.getKeyS3().isBlank()) {
            throw new IllegalStateException("La guia aun no tiene un objeto asociado en S3.");
        }
        return guia.getKeyS3();
    }

    private GuiaDespacho obtenerGuia(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("La guia debe ser valida.");
        }
        return guiaDespachoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La guia indicada no existe."));
    }

    private Transportista obtenerTransportista(Long transportistaId) {
        if (transportistaId == null || transportistaId <= 0) {
            throw new IllegalArgumentException("El transportista debe ser valido.");
        }
        return transportistaRepository.findById(transportistaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El transportista indicado no existe."));
    }

    private void validarPermisoTransportista(GuiaDespacho guia, Long transportistaId) {
        if (!guia.getTransportista().getId().equals(transportistaId)) {
            throw new SecurityException("El transportista no tiene permisos para descargar esta guia.");
        }
    }

    private GuiaArchivoResponse respuesta(GuiaDespacho guia, String bucket, String key, String rutaEfs) {
        return new GuiaArchivoResponse(guia.getId(), bucket, key, guia.getNombreArchivo(), rutaEfs);
    }

    private String obtenerBucket() {
        if (guiasBucket == null || guiasBucket.trim().isEmpty()) {
            throw new IllegalStateException("Debe configurar AWS_S3_GUIAS_BUCKET para usar S3.");
        }
        return guiasBucket.trim();
    }

    private void validarGuia(GuiaDespachoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La guia de despacho es obligatoria.");
        }
        if (esTextoVacio(request.numeroPedido())) {
            throw new IllegalArgumentException("El numero de pedido es obligatorio.");
        }
        if (esTextoVacio(request.clienteNombre())) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        if (esTextoVacio(request.direccionDestino())) {
            throw new IllegalArgumentException("La direccion de destino es obligatoria.");
        }
        if (esTextoVacio(request.comunaDestino())) {
            throw new IllegalArgumentException("La comuna de destino es obligatoria.");
        }
        if (esTextoVacio(request.descripcionCarga())) {
            throw new IllegalArgumentException("La descripcion de carga es obligatoria.");
        }
        if (request.pesoKg() == null || request.pesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a cero.");
        }
    }

    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar una guia de despacho.");
        }
    }

    private boolean esTextoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String slug(String valor) {
        String normalizado = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalizado.isBlank() ? "transportista" : normalizado;
    }
}
