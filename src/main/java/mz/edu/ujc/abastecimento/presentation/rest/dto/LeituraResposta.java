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
        String leitorNome = null;
        try {
            if (leitura.getUtilizador() != null) {
                leitorNome = leitura.getUtilizador().getNome();
            }
        } catch (Exception ignored) {
            // Caso o proxy não esteja inicializado fora de sessão
        }

        return new LeituraResposta(
                leitura.getId(),
                leitura.getContador().getId(),
                leitura.getContador().getNumeroSerie(),
                leitura.getContador().getLigacao().getCodigo(),
                leitura.getContador().getLigacao().getCliente().getNome(),
                leitura.getDataLeitura(),
                leitura.getValorM3(),
                leitura.getTipo(),
                leitorNome,
                leitura.getObservacao()
        );
    }
}
