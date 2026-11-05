package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataClienteRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataContadorRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLeituraRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLigacaoRepository;
import mz.edu.ujc.abastecimento.presentation.rest.dto.DashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardService {

    private final SpringDataClienteRepository clientes;
    private final SpringDataLigacaoRepository ligacoes;
    private final SpringDataContadorRepository contadores;
    private final SpringDataLeituraRepository leituras;

    public DashboardService(
            SpringDataClienteRepository clientes,
            SpringDataLigacaoRepository ligacoes,
            SpringDataContadorRepository contadores,
            SpringDataLeituraRepository leituras
    ) {
        this.clientes = clientes;
        this.ligacoes = ligacoes;
        this.contadores = contadores;
        this.leituras = leituras;
    }

    @Transactional(readOnly = true)
    public DashboardResponse resumo() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fim = hoje.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        BigDecimal volume = leituras.sumValorM3ByDataLeituraBetween(inicio, fim);

        return new DashboardResponse(
                clientes.count(),
                ligacoes.countByEstado("ACTIVA"),
                contadores.countByEstado("ACTIVO"),
                leituras.countByDataLeituraBetween(inicio, fim),
                volume == null ? BigDecimal.ZERO : volume
        );
    }
}
