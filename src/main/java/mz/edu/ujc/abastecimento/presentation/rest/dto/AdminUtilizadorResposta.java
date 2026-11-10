package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Utilizador;

import java.util.Set;
import java.util.UUID;

public record AdminUtilizadorResposta(
        UUID id,
        String nome,
        String nomeUtilizador,
        String email,
        Set<String> perfis,
        boolean activo
) {

    public static AdminUtilizadorResposta de(Utilizador utilizador) {
        return new AdminUtilizadorResposta(
                utilizador.getId(),
                utilizador.getNome(),
                utilizador.getNomeUtilizador(),
                utilizador.getEmail(),
                utilizador.getCodigosPerfis(),
                utilizador.isAtivo()
        );
    }
}
