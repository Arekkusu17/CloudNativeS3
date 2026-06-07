package cl.duoc.cloudnative.guiasdespacho.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transportistas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String rut;

    @Column(name = "email_contacto", nullable = false, length = 160)
    private String emailContacto;

    public Transportista(String nombre, String rut, String emailContacto) {
        this.nombre = nombre;
        this.rut = rut;
        this.emailContacto = emailContacto;
    }
}
