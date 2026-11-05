package mz.edu.ujc.abastecimento.presentation.rest.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalClientes,
        long ligacoesActivas,
        long contadoresActivos,
        long leiturasEsteMes,
        BigDecimal volumeEsteMesM3
) {
}
