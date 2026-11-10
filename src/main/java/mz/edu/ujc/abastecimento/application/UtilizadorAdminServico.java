package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.domain.exception.ClienteNaoEncontradoException;
import mz.edu.ujc.abastecimento.domain.exception.RegraNegocioException;
import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.domain.model.Perfil;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.ClienteRepositorioAdaptador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataPerfilRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataUtilizadorRepositorio;
import mz.edu.ujc.abastecimento.presentation.rest.dto.CriarUtilizadorPedido;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UtilizadorAdminServico {

    private final SpringDataUtilizadorRepositorio utilizadores;
    private final SpringDataPerfilRepositorio perfis;
    private final ClienteRepositorioAdaptador clientes;
    private final PasswordEncoder passwordEncoder;

    public UtilizadorAdminServico(
            SpringDataUtilizadorRepositorio utilizadores,
            SpringDataPerfilRepositorio perfis,
            ClienteRepositorioAdaptador clientes,
            PasswordEncoder passwordEncoder
    ) {
        this.utilizadores = utilizadores;
        this.perfis = perfis;
        this.clientes = clientes;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Utilizador> listar() {
        return utilizadores.findAllByOrderByNomeUtilizadorAsc();
    }

    @Transactional
    public Utilizador criar(CriarUtilizadorPedido pedido) {
        String username = pedido.nomeUtilizador().trim();
        String email = pedido.email().trim();
        if (utilizadores.existsByNomeUtilizadorIgnoreCase(username)) {
            throw new RegraNegocioException("Este nome de utilizador ja esta em uso.");
        }
        if (utilizadores.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Este email ja esta registado.");
        }

        Set<String> codigos = pedido.perfis().stream()
                .map(codigo -> codigo.toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        List<Perfil> perfisEncontrados = perfis.findAllByCodigoIn(codigos);
        if (perfisEncontrados.size() != codigos.size()) {
            throw new RegraNegocioException("Um ou mais perfis indicados sao invalidos.");
        }

        Cliente cliente = null;
        if (pedido.clienteId() != null) {
            cliente = clientes.procurarPorId(pedido.clienteId())
                    .orElseThrow(() -> new ClienteNaoEncontradoException(pedido.clienteId()));
        } else if (codigos.contains("CLIENTE")) {
            throw new RegraNegocioException(
                    "O perfil CLIENTE exige a associacao a um cliente existente."
            );
        }

        Utilizador novo = new Utilizador(
                pedido.nome().trim(),
                username,
                email,
                passwordEncoder.encode(pedido.senha()),
                Set.copyOf(perfisEncontrados)
        );
        Utilizador guardado = utilizadores.save(novo);

        if (cliente != null) {
            cliente.associarUtilizador(guardado);
            clientes.guardar(cliente);
        }

        return guardado;
    }
}
