package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Contador;

import java.time.LocalDate;
import java.util.UUID;

public record ContadorResponse(
        UUID id,
        String numeroSerie,
        String modelo,
        String codigoLigacao,
        String cliente,
        LocalDate dataInstalacao,
        String estado
) {

    public static ContadorResponse de(Contador contador) {
        return new ContadorResponse(
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
