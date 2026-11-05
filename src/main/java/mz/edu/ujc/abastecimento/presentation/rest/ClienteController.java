package mz.edu.ujc.abastecimento.presentation.rest;

import jakarta.validation.Valid;
import mz.edu.ujc.abastecimento.application.ClienteService;
import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.presentation.rest.dto.ClienteResponse;
import mz.edu.ujc.abastecimento.presentation.rest.dto.RegistarClienteRequest;
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
public class ClienteController {

    private final ClienteService clientes;

    public ClienteController(ClienteService clientes) {
        this.clientes = clientes;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> registar(
            @Valid @RequestBody RegistarClienteRequest pedido
    ) {
        Cliente cliente = clientes.registar(
                pedido.nome(),
                pedido.documento(),
                pedido.email(),
                pedido.telefone()
        );
        ClienteResponse resposta = ClienteResponse.de(cliente);
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + resposta.id()))
                .body(resposta);
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return clientes.listar().stream().map(ClienteResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse procurarPorId(@PathVariable UUID id) {
        return ClienteResponse.de(clientes.procurarPorId(id));
    }
}
