package cl.duoc.cloudnative.guiasdespacho.service;

import cl.duoc.cloudnative.guiasdespacho.model.GuiaDespacho;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Component
public class GuiaTextoGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generarTxt(GuiaDespacho guia) {
        return construirContenido(guia).getBytes(StandardCharsets.UTF_8);
    }

    private String construirContenido(GuiaDespacho guia) {
        String separador = System.lineSeparator();
        return "Guia de despacho: " + guia.getNumeroGuia() + separador
                + "Fecha emision: " + guia.getFechaEmision().format(DATE_FORMAT) + separador
                + "Transportista: " + guia.getTransportista().getNombre() + separador
                + "RUT transportista: " + guia.getTransportista().getRut() + separador
                + "Pedido: " + guia.getPedido().getNumeroPedido() + separador
                + "Cliente: " + guia.getPedido().getClienteNombre() + separador
                + "Destino: " + guia.getPedido().getDireccionDestino() + ", "
                + guia.getPedido().getComunaDestino() + separador
                + "Carga: " + guia.getPedido().getDescripcionCarga() + separador
                + "Peso KG: " + formatearMonto(guia.getPedido().getPesoKg()) + separador;
    }

    private String formatearMonto(BigDecimal monto) {
        return monto.stripTrailingZeros().toPlainString();
    }
}
