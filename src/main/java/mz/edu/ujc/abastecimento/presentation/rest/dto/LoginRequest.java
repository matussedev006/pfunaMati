package mz.edu.ujc.abastecimento.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String nomeUtilizador,
        @NotBlank String senha
) {
}
