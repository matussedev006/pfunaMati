package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Utilizador;

import java.util.Set;
import java.util.UUID;

public record AdminUtilizadorResponse(
        UUID id,
        String nome,
        String nomeUtilizador,
        String email,
        Set<String> perfis,
        boolean activo
) {

    public static AdminUtilizadorResponse de(Utilizador utilizador) {
        return new AdminUtilizadorResponse(
                utilizador.getId(),
                utilizador.getNome(),
                utilizador.getUsername(),
                utilizador.getEmail(),
                utilizador.getCodigosPerfis(),
                utilizador.isEnabled()
        );
    }
}
