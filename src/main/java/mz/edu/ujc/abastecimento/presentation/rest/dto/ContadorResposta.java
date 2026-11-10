package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Contador;

import java.time.LocalDate;
import java.util.UUID;

public record ContadorResposta(
        UUID id,
        String numeroSerie,
        String modelo,
        String codigoLigacao,
        String cliente,
        LocalDate dataInstalacao,
        String estado
) {

    public static ContadorResposta de(Contador contador) {
        return new ContadorResposta(
                contador.getId(),
                contador.getNumeroSerie(),
                contador.getModelo(),
                contador.getLigacao().getCodigo(),
                contador.getLigacao().getCliente().getNome(),
                contador.getDataInstalacao(),
                contador.getEstado()
        );
    }
}
