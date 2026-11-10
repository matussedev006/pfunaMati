package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataClienteRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataContadorRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLeituraRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLigacaoRepositorio;
import mz.edu.ujc.abastecimento.presentation.rest.dto.DashboardResposta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardServico {

    private final SpringDataClienteRepositorio clientes;
    private final SpringDataLigacaoRepositorio ligacoes;
    private final SpringDataContadorRepositorio contadores;
    private final SpringDataLeituraRepositorio leituras;

    public DashboardServico(
            SpringDataClienteRepositorio clientes,
            SpringDataLigacaoRepositorio ligacoes,
            SpringDataContadorRepositorio contadores,
            SpringDataLeituraRepositorio leituras
    ) {
        this.clientes = clientes;
        this.ligacoes = ligacoes;
        this.contadores = contadores;
        this.leituras = leituras;
    }

    @Transactional(readOnly = true)
    public DashboardResposta resumo() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fim = hoje.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        BigDecimal volume = leituras.sumValorM3ByDataLeituraBetween(inicio, fim);

        return new DashboardResposta(
                clientes.count(),
                ligacoes.countByEstado("ACTIVA"),
                contadores.countByEstado("ACTIVO"),
                leituras.countByDataLeituraBetween(inicio, fim),
                volume == null ? BigDecimal.ZERO : volume
        );
    }
}
