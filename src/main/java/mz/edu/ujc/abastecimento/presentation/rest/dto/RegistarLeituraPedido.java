package mz.edu.ujc.abastecimento.presentation.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegistarLeituraPedido(
        @NotNull UUID contadorId,
        @NotNull LocalDateTime dataLeitura,
        @NotNull @DecimalMin("0.000") BigDecimal valorM3,
        @NotNull @Pattern(regexp = "REAL|ESTIMADA") String tipo,
        @Size(max = 500) String observacao
) {
}
