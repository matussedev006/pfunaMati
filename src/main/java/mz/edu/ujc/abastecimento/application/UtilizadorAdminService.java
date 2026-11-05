package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.domain.exception.ClienteNaoEncontradoException;
import mz.edu.ujc.abastecimento.domain.exception.RegraNegocioException;
import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.domain.model.Perfil;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.ClienteRepositoryAdapter;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataPerfilRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataUtilizadorRepository;
import mz.edu.ujc.abastecimento.presentation.rest.dto.CriarUtilizadorRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UtilizadorAdminService {

    private final SpringDataUtilizadorRepository utilizadores;
    private final SpringDataPerfilRepository perfis;
    private final ClienteRepositoryAdapter clientes;
    private final PasswordEncoder passwordEncoder;

    public UtilizadorAdminService(
            SpringDataUtilizadorRepository utilizadores,
            SpringDataPerfilRepository perfis,
            ClienteRepositoryAdapter clientes,
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
    public Utilizador criar(CriarUtilizadorRequest pedido) {
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
            throw new RegraNegocioException("Um ou mais perfis seleccionados nao existem.");
        }

        boolean cliente = codigos.contains("CLIENTE");
        if (cliente != (pedido.clienteId() != null)) {
            throw new RegraNegocioException(
                    "Associe uma conta CLIENTE a um registo de cliente e nao associe clientes a outras roles."
            );
        }

        Utilizador utilizador = utilizadores.save(new Utilizador(
                pedido.nome().trim(),
                username,
                email,
                passwordEncoder.encode(pedido.senha()),
                Set.copyOf(perfisEncontrados)
        ));

        if (cliente) {
            Cliente registoCliente = clientes.procurarPorId(pedido.clienteId())
                    .orElseThrow(() -> new ClienteNaoEncontradoException(pedido.clienteId()));
            registoCliente.associarUtilizador(utilizador);
            clientes.guardar(registoCliente);
        }
        return utilizador;
    }
}
