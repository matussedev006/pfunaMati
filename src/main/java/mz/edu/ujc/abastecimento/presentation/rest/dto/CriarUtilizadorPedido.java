package mz.edu.ujc.abastecimento.presentation.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CriarUtilizadorPedido(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 60) String nomeUtilizador,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(min = 10, max = 72) String senha,
        @NotEmpty Set<@Pattern(regexp = "ADMIN|LEITOR|CLIENTE") String> perfis,
        UUID clienteId
) {
}
