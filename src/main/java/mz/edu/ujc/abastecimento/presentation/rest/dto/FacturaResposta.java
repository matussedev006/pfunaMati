package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Factura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FacturaResposta(
        UUID id,
        String numero,
        UUID clienteId,
        String clienteNome,
        String clienteDocumento,
        UUID ligacaoId,
        String ligacaoCodigo,
        String contadorNumeroSerie,
        String tarifaNome,
        LocalDate periodoInicio,
        LocalDate periodoFim,
        LocalDate dataEmissao,
        LocalDate dataVencimento,
        BigDecimal volumeM3,
        BigDecimal precoPorM3,
        BigDecimal valorConsumo,
        BigDecimal taxaFixa,
        BigDecimal valorTotal,
        String estado,
        LocalDateTime dataCriacao
) {
    public static FacturaResposta de(Factura factura) {
        return new FacturaResposta(
                factura.getId(),
                factura.getNumero(),
                factura.getCliente().getId(),
                factura.getCliente().getNome(),
                factura.getCliente().getDocumento(),
                factura.getLigacao().getId(),
                factura.getLigacao().getCodigo(),
                factura.getConsumo().getContador().getNumeroSerie(),
                factura.getTarifa().getNome(),
                factura.getPeriodoInicio(),
                factura.getPeriodoFim(),
                factura.getDataEmissao(),
                factura.getDataVencimento(),
                factura.getConsumo().getVolumeM3(),
                factura.getTarifa().getPrecoPorM3(),
                factura.getValorConsumo(),
                factura.getTaxaFixa(),
                factura.getValorTotal(),
                factura.getEstado(),
                factura.getDataCriacao()
        );
    }
}
