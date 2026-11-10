package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Cliente;

import java.time.Instant;
import java.util.UUID;

public record ClienteResposta(
        UUID id,
        String nome,
        String documento,
        String email,
        String telefone,
        Instant dataRegisto
) {

    public static ClienteResposta de(Cliente cliente) {
        return new ClienteResposta(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getDataRegisto()
        );
    }
}
