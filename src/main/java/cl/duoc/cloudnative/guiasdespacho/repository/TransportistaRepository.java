package cl.duoc.cloudnative.guiasdespacho.repository;

import cl.duoc.cloudnative.guiasdespacho.model.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportistaRepository extends JpaRepository<Transportista, Long> {
    boolean existsByRut(String rut);
}
