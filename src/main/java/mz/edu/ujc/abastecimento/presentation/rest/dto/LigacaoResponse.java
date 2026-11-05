package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Ligacao;

import java.time.LocalDate;
import java.util.UUID;

public record LigacaoResponse(
        UUID id,
        String codigo,
        String cliente,
        String endereco,
        String bairro,
        String cidade,
        String provincia,
        LocalDate dataActivacao,
        String estado
) {

    public static LigacaoResponse de(Ligacao ligacao) {
        return new LigacaoResponse(
                ligacao.getId(),
                ligacao.getCodigo(),
                ligacao.getCliente().getNome(),
                ligacao.getEndereco(),
                ligacao.getBairro(),
                ligacao.getCidade(),
                ligacao.getProvincia(),
                ligacao.getDataActivacao(),
                ligacao.getEstado()
        );
    }
}
