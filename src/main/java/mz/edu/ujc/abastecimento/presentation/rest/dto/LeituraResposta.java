package mz.edu.ujc.abastecimento.presentation.rest.dto;

import mz.edu.ujc.abastecimento.domain.model.Leitura;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeituraResposta(
        UUID id,
        UUID contadorId,
        String numeroSerie,
        String codigoLigacao,
        String cliente,
        LocalDateTime dataLeitura,
        BigDecimal valorM3,
        String tipo,
        String leitor,
        String observacao
) {

    public static LeituraResposta de(Leitura leitura) {
        return new LeituraResposta(
                leitura.getId(),
                leitura.getContador().getId(),
                leitura.getContador().getNumeroSerie(),
                leitura.getContador().getLigacao().getCodigo(),
                leitura.getContador().getLigacao().getCliente().getNome(),
                leitura.getDataLeitura(),
                leitura.getValorM3(),
                leitura.getTipo(),
                leitura.getUtilizador() == null ? null : leitura.getUtilizador().getNome(),
                leitura.getObservacao()
        );
    }
}
