package mz.edu.ujc.abastecimento.presentation.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GerarFacturaPedido(
        @NotNull(message = "O contador deve ser especificado.")
        UUID contadorId,
        UUID leituraInicialId,
        UUID leituraFinalId,
        UUID tarifaId,
        BigDecimal leituraInicialM3,
        BigDecimal leituraFinalM3,
        LocalDate periodoInicio,
        LocalDate periodoFim,
        LocalDate dataEmissao,
        LocalDate dataVencimento
) {
}
