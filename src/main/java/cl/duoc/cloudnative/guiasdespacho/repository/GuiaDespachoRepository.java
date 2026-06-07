package cl.duoc.cloudnative.guiasdespacho.repository;

import cl.duoc.cloudnative.guiasdespacho.model.GuiaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GuiaDespachoRepository extends JpaRepository<GuiaDespacho, Long> {
    List<GuiaDespacho> findByTransportistaIdAndFechaEmisionBetween(
            Long transportistaId,
            LocalDateTime inicio,
            LocalDateTime fin);
}
