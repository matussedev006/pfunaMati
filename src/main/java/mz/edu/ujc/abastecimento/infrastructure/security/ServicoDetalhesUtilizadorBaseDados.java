package mz.edu.ujc.abastecimento.infrastructure.security;

import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataUtilizadorRepositorio;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoDetalhesUtilizadorBaseDados implements UserDetailsService {

    private final SpringDataUtilizadorRepositorio utilizadores;

    public ServicoDetalhesUtilizadorBaseDados(SpringDataUtilizadorRepositorio utilizadores) {
        this.utilizadores = utilizadores;
    }

    @Override
    @Transactional(readOnly = true)
    public Utilizador loadUserByUsername(String username) {
        return carregarPorNomeUtilizador(username);
    }

    @Transactional(readOnly = true)
    public Utilizador carregarPorNomeUtilizador(String nomeUtilizador) {
        return utilizadores.findByNomeUtilizadorIgnoreCase(nomeUtilizador.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais invalidas."));
    }
}
