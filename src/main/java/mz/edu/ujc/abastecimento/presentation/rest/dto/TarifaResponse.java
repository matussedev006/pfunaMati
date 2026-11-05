package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Tarifa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TarifaResponse(
        UUID id,
        String nome,
        String descricao,
        BigDecimal taxaFixa,
        BigDecimal precoPorM3,
        LocalDate vigenteDesde,
        boolean activa
) {

    public static TarifaResponse de(Tarifa tarifa) {
        return new TarifaResponse(
                tarifa.getId(),
                tarifa.getNome(),
                tarifa.getDescricao(),
                tarifa.getTaxaFixa(),
                tarifa.getPrecoPorM3(),
                tarifa.getVigenteDesde(),
                tarifa.isActiva()
        );
    }
}
