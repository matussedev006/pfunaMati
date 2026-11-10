package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Utilizador;

import java.util.Set;

public record UtilizadorResposta(
        String nome,
        String nomeUtilizador,
        String email,
        Set<String> perfis
) {

    public static UtilizadorResposta de(Utilizador utilizador) {
        return new UtilizadorResposta(
                utilizador.getNome(),
                utilizador.getNomeUtilizador(),
                utilizador.getEmail(),
                utilizador.getCodigosPerfis()
        );
    }
}
