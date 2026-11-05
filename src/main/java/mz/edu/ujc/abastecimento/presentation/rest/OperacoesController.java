package mz.edu.ujc.abastecimento.presentation.rest;

import jakarta.validation.Valid;
import mz.edu.ujc.abastecimento.application.DashboardService;
import mz.edu.ujc.abastecimento.application.LeituraService;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataContadorRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLeituraRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLigacaoRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataTarifaRepository;
import mz.edu.ujc.abastecimento.presentation.rest.dto.ContadorResponse;
import mz.edu.ujc.abastecimento.presentation.rest.dto.DashboardResponse;
import mz.edu.ujc.abastecimento.presentation.rest.dto.LeituraResponse;
import mz.edu.ujc.abastecimento.presentation.rest.dto.LigacaoResponse;
import mz.edu.ujc.abastecimento.presentation.rest.dto.RegistarLeituraRequest;
import mz.edu.ujc.abastecimento.presentation.rest.dto.TarifaResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OperacoesController {

    private final DashboardService dashboard;
    private final SpringDataLigacaoRepository ligacoes;
    private final SpringDataContadorRepository contadores;
    private final SpringDataTarifaRepository tarifas;
    private final SpringDataLeituraRepository leituras;
    private final LeituraService leituraService;

    public OperacoesController(
            DashboardService dashboard,
            SpringDataLigacaoRepository ligacoes,
            SpringDataContadorRepository contadores,
            SpringDataTarifaRepository tarifas,
            SpringDataLeituraRepository leituras,
            LeituraService leituraService
    ) {
        this.dashboard = dashboard;
        this.ligacoes = ligacoes;
        this.contadores = contadores;
        this.tarifas = tarifas;
        this.leituras = leituras;
        this.leituraService = leituraService;
    }

    @GetMapping("/dashboard/resumo")
    public DashboardResponse resumo() {
        return dashboard.resumo();
    }

    @GetMapping("/ligacoes")
    public List<LigacaoResponse> listarLigacoes() {
        return ligacoes.findAllByOrderByCodigoAsc().stream()
                .map(LigacaoResponse::de)
                .toList();
    }

    @GetMapping("/contadores")
    public List<ContadorResponse> listarContadores() {
        return contadores.findAllByOrderByNumeroSerieAsc().stream()
                .map(ContadorResponse::de)
                .toList();
    }

    @GetMapping("/tarifas")
    public List<TarifaResponse> listarTarifas() {
        return tarifas.findAllByOrderByNomeAsc().stream()
                .map(TarifaResponse::de)
                .toList();
    }

    @GetMapping("/leituras")
    public List<LeituraResponse> listarLeituras() {
        return leituras.findTop50ByOrderByDataLeituraDesc().stream()
                .map(LeituraResponse::de)
                .toList();
    }

    @PostMapping("/leituras")
    public LeituraResponse registarLeitura(
            @Valid @RequestBody RegistarLeituraRequest pedido,
            @AuthenticationPrincipal Utilizador utilizador
    ) {
        return LeituraResponse.de(leituraService.registar(pedido, utilizador));
    }
}
