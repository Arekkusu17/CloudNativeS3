package cl.duoc.cloudnative.guiasdespacho.repository;

import cl.duoc.cloudnative.guiasdespacho.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    boolean existsByNumeroPedido(String numeroPedido);
}
