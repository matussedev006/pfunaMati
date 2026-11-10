package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Cliente;

import java.util.List;
import java.util.UUID;

public record PortalClienteResposta(
        UUID id,
        String nome,
        String documento,
        String email,
        String telefone,
        List<LigacaoResposta> ligacoes
) {

    public static PortalClienteResposta de(Cliente cliente, List<LigacaoResposta> ligacoes) {
        return new PortalClienteResposta(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getEmail(),
                cliente.getTelefone(),
                List.copyOf(ligacoes)
        );
    }
}
