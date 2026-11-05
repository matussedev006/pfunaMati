package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Contador;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataContadorRepository extends JpaRepository<Contador, UUID> {

    long countByEstado(String estado);

    @EntityGraph(attributePaths = {"ligacao", "ligacao.cliente"})
    List<Contador> findAllByOrderByNumeroSerieAsc();

    @EntityGraph(attributePaths = {"ligacao", "ligacao.cliente"})
    Optional<Contador> findByIdAndEstado(UUID id, String estado);
}
