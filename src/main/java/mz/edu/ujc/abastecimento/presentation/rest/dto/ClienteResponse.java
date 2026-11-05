package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Cliente;

import java.time.Instant;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String documento,
        String email,
        String telefone,
        Instant dataRegisto
) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getDataRegisto()
        );
    }
}
