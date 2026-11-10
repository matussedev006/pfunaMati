package mz.edu.ujc.abastecimento.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginPedido(
        @NotBlank String nomeUtilizador,
        @NotBlank String senha
) {
}
