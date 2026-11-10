package mz.edu.ujc.abastecimento.presentation.rest;

import jakarta.validation.Valid;
import mz.edu.ujc.abastecimento.application.FacturacaoServico;
import mz.edu.ujc.abastecimento.presentation.rest.dto.FacturaResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.FacturacaoResumoResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.GerarFacturaPedido;
import mz.edu.ujc.abastecimento.presentation.rest.dto.NotificacaoVencimentoResposta;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facturas")
public class FacturaControlador {

    private final FacturacaoServico facturacaoServico;

    public FacturaControlador(FacturacaoServico facturacaoServico) {
        this.facturacaoServico = facturacaoServico;
    }

    @GetMapping
    public List<FacturaResposta> listar() {
        return facturacaoServico.listar();
    }

    @GetMapping("/resumo")
    public FacturacaoResumoResposta resumo() {
        return facturacaoServico.resumo();
    }

    @PostMapping("/gerar")
    @ResponseStatus(HttpStatus.CREATED)
    public FacturaResposta gerar(@Valid @RequestBody GerarFacturaPedido pedido) {
        return facturacaoServico.gerar(pedido);
    }

    @PostMapping("/{id}/liquidar")
    public FacturaResposta liquidar(@PathVariable UUID id) {
        return facturacaoServico.marcarComoPaga(id);
    }

    @GetMapping("/alertas")
    public List<NotificacaoVencimentoResposta> alertas() {
        return facturacaoServico.obterAlertasVencimento();
    }

    @PostMapping("/{id}/lembrete")
    public Map<String, String> enviarLembrete(@PathVariable UUID id) {
        String mensagem = facturacaoServico.enviarLembrete(id);
        return Map.of("mensagem", mensagem);
    }
}
