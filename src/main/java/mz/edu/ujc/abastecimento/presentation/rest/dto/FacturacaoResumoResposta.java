package mz.edu.ujc.abastecimento.presentation.rest.dto;

import java.math.BigDecimal;

public record FacturacaoResumoResposta(
        BigDecimal totalFacturado,
        BigDecimal totalPendente,
        BigDecimal totalPago,
        long totalFacturas,
        long facturasPendentes,
        long facturasPagas,
        BigDecimal volumeTotalM3
) {
}
