package mz.edu.ujc.abastecimento.presentation.rest;

import jakarta.validation.Valid;
import mz.edu.ujc.abastecimento.application.DashboardServico;
import mz.edu.ujc.abastecimento.application.LeituraServico;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataContadorRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLeituraRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLigacaoRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataTarifaRepositorio;
import mz.edu.ujc.abastecimento.presentation.rest.dto.ContadorResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.DashboardResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.LeituraResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.LigacaoResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.RegistarLeituraPedido;
import mz.edu.ujc.abastecimento.presentation.rest.dto.TarifaResposta;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OperacoesControlador {

    private final DashboardServico dashboard;
    private final SpringDataLigacaoRepositorio ligacoes;
    private final SpringDataContadorRepositorio contadores;
    private final SpringDataTarifaRepositorio tarifas;
    private final SpringDataLeituraRepositorio leituras;
    private final LeituraServico leituraServico;

    public OperacoesControlador(
            DashboardServico dashboard,
            SpringDataLigacaoRepositorio ligacoes,
            SpringDataContadorRepositorio contadores,
            SpringDataTarifaRepositorio tarifas,
            SpringDataLeituraRepositorio leituras,
            LeituraServico leituraServico
    ) {
        this.dashboard = dashboard;
        this.ligacoes = ligacoes;
        this.contadores = contadores;
        this.tarifas = tarifas;
        this.leituras = leituras;
        this.leituraServico = leituraServico;
    }

    @GetMapping("/dashboard/resumo")
    public DashboardResposta resumo() {
        return dashboard.resumo();
    }

    @GetMapping("/ligacoes")
    public List<LigacaoResposta> listarLigacoes() {
        return ligacoes.findAllByOrderByCodigoAsc().stream()
                .map(LigacaoResposta::de)
                .toList();
    }

    @GetMapping("/contadores")
    public List<ContadorResposta> listarContadores() {
        return contadores.findAllByOrderByNumeroSerieAsc().stream()
                .map(ContadorResposta::de)
                .toList();
    }

    @GetMapping("/tarifas")
    public List<TarifaResposta> listarTarifas() {
        return tarifas.findAllByOrderByNomeAsc().stream()
                .map(TarifaResposta::de)
                .toList();
    }

    @GetMapping("/leituras")
    public List<LeituraResposta> listarLeituras() {
        return leituraServico.ultimas().stream()
                .map(LeituraResposta::de)
                .toList();
    }

    @PostMapping("/leituras")
    public LeituraResposta registarLeitura(
            @Valid @RequestBody RegistarLeituraPedido pedido,
            @AuthenticationPrincipal Utilizador utilizador
    ) {
        return LeituraResposta.de(leituraServico.registar(pedido, utilizador));
    }
}
