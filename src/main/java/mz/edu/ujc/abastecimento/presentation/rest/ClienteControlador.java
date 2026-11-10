package mz.edu.ujc.abastecimento.presentation.rest;

import jakarta.validation.Valid;
import mz.edu.ujc.abastecimento.application.ClienteServico;
import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.presentation.rest.dto.ClienteResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.RegistarClientePedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteControlador {

    private final ClienteServico clientes;

    public ClienteControlador(ClienteServico clientes) {
        this.clientes = clientes;
    }

    @PostMapping
    public ResponseEntity<ClienteResposta> registar(
            @Valid @RequestBody RegistarClientePedido pedido
    ) {
        Cliente cliente = clientes.registar(
                pedido.nome(),
                pedido.documento(),
                pedido.email(),
                pedido.telefone()
        );
        ClienteResposta resposta = ClienteResposta.de(cliente);
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + resposta.id()))
                .body(resposta);
    }

    @GetMapping
    public List<ClienteResposta> listar() {
        return clientes.listar().stream().map(ClienteResposta::de).toList();
    }

    @GetMapping("/{id}")
    public ClienteResposta procurarPorId(@PathVariable UUID id) {
        return ClienteResposta.de(clientes.procurarPorId(id));
    }
}
