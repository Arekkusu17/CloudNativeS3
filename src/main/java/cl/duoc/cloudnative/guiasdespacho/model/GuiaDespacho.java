package cl.duoc.cloudnative.guiasdespacho.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "guias_despacho")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuiaDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id", nullable = false)
    private Transportista transportista;

    @Column(name = "numero_guia", nullable = false, unique = true, length = 40)
    private String numeroGuia;

    @Column(name = "nombre_archivo", nullable = false, length = 160)
    private String nombreArchivo;

    @Column(name = "ruta_efs", length = 500)
    private String rutaEfs;

    @Column(name = "bucket_s3", length = 120)
    private String bucketS3;

    @Column(name = "key_s3", length = 500)
    private String keyS3;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "fecha_subida_s3")
    private LocalDateTime fechaSubidaS3;

    @Column(nullable = false, length = 30)
    private String estado;

    public GuiaDespacho(Pedido pedido, Transportista transportista, String numeroGuia, String nombreArchivo) {
        this.pedido = pedido;
        this.transportista = transportista;
        this.numeroGuia = numeroGuia;
        this.nombreArchivo = nombreArchivo;
        this.estado = "GENERADA";
    }

    @PrePersist
    void asignarFechaEmision() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "GENERADA";
        }
    }

    public void actualizarTransportista(Transportista transportista) {
        this.transportista = transportista;
    }

    public void actualizarArchivoEfs(String nombreArchivo, String rutaEfs) {
        this.nombreArchivo = nombreArchivo;
        this.rutaEfs = rutaEfs;
    }

    public void actualizarS3(String bucketS3, String keyS3) {
        this.bucketS3 = bucketS3;
        this.keyS3 = keyS3;
        this.fechaSubidaS3 = LocalDateTime.now();
        this.estado = "SUBIDA_S3";
    }

    public void limpiarS3() {
        this.bucketS3 = null;
        this.keyS3 = null;
        this.fechaSubidaS3 = null;
        this.estado = "GENERADA";
    }
}
