package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Cliente;

import java.util.List;
import java.util.UUID;

public record PortalClienteResponse(
        UUID id,
        String nome,
        String documento,
        String email,
        String telefone,
        List<LigacaoResponse> ligacoes
) {

    public static PortalClienteResponse de(Cliente cliente, List<LigacaoResponse> ligacoes) {
        return new PortalClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getEmail(),
                cliente.getTelefone(),
                List.copyOf(ligacoes)
        );
    }
}
