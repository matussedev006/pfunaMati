package mz.edu.ujc.abastecimento.presentation.rest;

import mz.edu.ujc.abastecimento.application.FacturacaoServico;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataClienteRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLeituraRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLigacaoRepositorio;
import mz.edu.ujc.abastecimento.presentation.rest.dto.FacturaResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.LeituraResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.LigacaoResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.NotificacaoVencimentoResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.PortalClienteResposta;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/portal")
public class PortalClienteControlador {

    private final SpringDataClienteRepositorio clientes;
    private final SpringDataLigacaoRepositorio ligacoes;
    private final SpringDataLeituraRepositorio leituras;
    private final FacturacaoServico facturacaoServico;

    public PortalClienteControlador(
            SpringDataClienteRepositorio clientes,
            SpringDataLigacaoRepositorio ligacoes,
            SpringDataLeituraRepositorio leituras,
            FacturacaoServico facturacaoServico
    ) {
        this.clientes = clientes;
        this.ligacoes = ligacoes;
        this.leituras = leituras;
        this.facturacaoServico = facturacaoServico;
    }

    @GetMapping("/clientes")
    public List<PortalClienteResposta> meusClientes(
            @AuthenticationPrincipal Utilizador utilizador
    ) {
        Map<UUID, List<LigacaoResposta>> ligacoesPorCliente =
                ligacoes.findDistinctByCliente_Utilizadores_IdOrderByCodigoAsc(utilizador.getId())
                        .stream()
                        .collect(Collectors.groupingBy(
                                ligacao -> ligacao.getCliente().getId(),
                                Collectors.mapping(LigacaoResposta::de, Collectors.toList())
                        ));

        return clientes.findDistinctByUtilizadores_Id(utilizador.getId()).stream()
                .map(cliente -> PortalClienteResposta.de(
                        cliente,
                        ligacoesPorCliente.getOrDefault(cliente.getId(), List.of())
                ))
                .toList();
    }

    @GetMapping("/leituras")
    public List<LeituraResposta> minhasLeituras(
            @AuthenticationPrincipal Utilizador utilizador
    ) {
        return leituras.findTop50DistinctByContador_Ligacao_Cliente_Utilizadores_IdOrderByDataLeituraDesc(
                        utilizador.getId()
                ).stream()
                .map(LeituraResposta::de)
                .toList();
    }

    @GetMapping("/facturas")
    public List<FacturaResposta> minhasFacturas(
            @AuthenticationPrincipal Utilizador utilizador
    ) {
        return facturacaoServico.listarPorUtilizador(utilizador.getId());
    }

    @GetMapping("/alertas")
    public List<NotificacaoVencimentoResposta> meusAlertas(
            @AuthenticationPrincipal Utilizador utilizador
    ) {
        return facturacaoServico.obterAlertasCliente(utilizador.getId());
    }
}
