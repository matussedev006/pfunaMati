package mz.edu.ujc.abastecimento.presentation.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistarClientePedido(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 40) String documento,
        @NotBlank @Email @Size(max = 160) String email,
        @Size(max = 30) String telefone
) {
}
