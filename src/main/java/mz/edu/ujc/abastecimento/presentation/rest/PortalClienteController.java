package mz.edu.ujc.abastecimento.presentation.rest;

import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataClienteRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLeituraRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLigacaoRepository;
import mz.edu.ujc.abastecimento.presentation.rest.dto.LeituraResponse;
import mz.edu.ujc.abastecimento.presentation.rest.dto.LigacaoResponse;
import mz.edu.ujc.abastecimento.presentation.rest.dto.PortalClienteResponse;
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
public class PortalClienteController {

    private final SpringDataClienteRepository clientes;
    private final SpringDataLigacaoRepository ligacoes;
    private final SpringDataLeituraRepository leituras;

    public PortalClienteController(
            SpringDataClienteRepository clientes,
            SpringDataLigacaoRepository ligacoes,
            SpringDataLeituraRepository leituras
    ) {
        this.clientes = clientes;
        this.ligacoes = ligacoes;
        this.leituras = leituras;
    }

    @GetMapping("/clientes")
    public List<PortalClienteResponse> meusClientes(
            @AuthenticationPrincipal Utilizador utilizador
    ) {
        Map<UUID, List<LigacaoResponse>> ligacoesPorCliente =
                ligacoes.findDistinctByCliente_Utilizadores_IdOrderByCodigoAsc(utilizador.getId())
                        .stream()
                        .collect(Collectors.groupingBy(
                                ligacao -> ligacao.getCliente().getId(),
                                Collectors.mapping(LigacaoResponse::de, Collectors.toList())
                        ));

        return clientes.findDistinctByUtilizadores_Id(utilizador.getId()).stream()
                .map(cliente -> PortalClienteResponse.de(
                        cliente,
                        ligacoesPorCliente.getOrDefault(cliente.getId(), List.of())
                ))
                .toList();
    }

    @GetMapping("/leituras")
    public List<LeituraResponse> minhasLeituras(
            @AuthenticationPrincipal Utilizador utilizador
    ) {
        return leituras
                .findTop50DistinctByContador_Ligacao_Cliente_Utilizadores_IdOrderByDataLeituraDesc(
                        utilizador.getId()
                )
                .stream()
                .map(LeituraResponse::de)
                .toList();
    }
}
