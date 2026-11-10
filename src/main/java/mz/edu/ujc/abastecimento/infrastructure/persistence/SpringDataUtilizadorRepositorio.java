package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUtilizadorRepositorio extends JpaRepository<Utilizador, UUID> {

    @EntityGraph(attributePaths = "perfis")
    Optional<Utilizador> findByNomeUtilizadorIgnoreCase(String nomeUtilizador);

    @EntityGraph(attributePaths = "perfis")
    List<Utilizador> findAllByOrderByNomeUtilizadorAsc();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNomeUtilizadorIgnoreCase(String nomeUtilizador);
}
