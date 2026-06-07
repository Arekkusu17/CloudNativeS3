package cl.duoc.cloudnative.guiasdespacho.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", nullable = false, unique = true, length = 40)
    private String numeroPedido;

    @Column(name = "cliente_nombre", nullable = false, length = 120)
    private String clienteNombre;

    @Column(name = "direccion_destino", nullable = false, length = 220)
    private String direccionDestino;

    @Column(name = "comuna_destino", nullable = false, length = 100)
    private String comunaDestino;

    @Column(name = "descripcion_carga", nullable = false, length = 300)
    private String descripcionCarga;

    @Column(name = "peso_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    public Pedido(
            String numeroPedido,
            String clienteNombre,
            String direccionDestino,
            String comunaDestino,
            String descripcionCarga,
            BigDecimal pesoKg) {
        this.numeroPedido = numeroPedido;
        this.clienteNombre = clienteNombre;
        this.direccionDestino = direccionDestino;
        this.comunaDestino = comunaDestino;
        this.descripcionCarga = descripcionCarga;
        this.pesoKg = pesoKg;
    }

    @PrePersist
    void asignarFechaPedido() {
        if (fechaPedido == null) {
            fechaPedido = LocalDateTime.now();
        }
    }

    public void actualizarDatos(
            String clienteNombre,
            String direccionDestino,
            String comunaDestino,
            String descripcionCarga,
            BigDecimal pesoKg) {
        this.clienteNombre = clienteNombre;
        this.direccionDestino = direccionDestino;
        this.comunaDestino = comunaDestino;
        this.descripcionCarga = descripcionCarga;
        this.pesoKg = pesoKg;
    }
}
