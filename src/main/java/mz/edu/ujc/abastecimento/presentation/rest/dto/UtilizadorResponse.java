package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Utilizador;

import java.util.Set;

public record UtilizadorResponse(
        String nome,
        String nomeUtilizador,
        String email,
        Set<String> perfis
) {

    public static UtilizadorResponse de(Utilizador utilizador) {
        return new UtilizadorResponse(
                utilizador.getNome(),
                utilizador.getUsername(),
                utilizador.getEmail(),
                utilizador.getCodigosPerfis()
        );
    }
}
