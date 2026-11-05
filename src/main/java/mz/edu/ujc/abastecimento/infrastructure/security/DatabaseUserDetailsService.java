package mz.edu.ujc.abastecimento.infrastructure.security;

import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataUtilizadorRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final SpringDataUtilizadorRepository utilizadores;

    public DatabaseUserDetailsService(SpringDataUtilizadorRepository utilizadores) {
        this.utilizadores = utilizadores;
    }

    @Override
    @Transactional(readOnly = true)
    public Utilizador loadUserByUsername(String username) {
        return utilizadores.findByNomeUtilizadorIgnoreCase(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais invalidas."));
    }
}
