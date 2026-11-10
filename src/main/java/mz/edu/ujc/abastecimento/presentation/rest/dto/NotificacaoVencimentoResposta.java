package mz.edu.ujc.abastecimento.presentation.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record NotificacaoVencimentoResposta(
        UUID facturaId,
        String facturaNumero,
        UUID clienteId,
        String clienteNome,
        String clienteEmail,
        String clienteTelefone,
        String ligacaoCodigo,
        BigDecimal valorTotal,
        LocalDate dataVencimento,
        long diasRestantes,
        String nivel,
        String mensagem
) {
}
