package mz.edu.ujc.abastecimento.presentation.rest.dto;

import java.time.Instant;

public record ErroApi(Instant timestamp, int status, String erro, String mensagem) {
}
