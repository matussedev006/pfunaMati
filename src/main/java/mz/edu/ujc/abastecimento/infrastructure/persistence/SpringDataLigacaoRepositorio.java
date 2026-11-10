package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Ligacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLigacaoRepositorio extends JpaRepository<Ligacao, UUID> {

    long countByEstado(String estado);

    @EntityGraph(attributePaths = "cliente")
    List<Ligacao> findAllByOrderByCodigoAsc();

    @EntityGraph(attributePaths = "cliente")
    List<Ligacao> findDistinctByCliente_Utilizadores_IdOrderByCodigoAsc(UUID utilizadorId);

    @EntityGraph(attributePaths = "cliente")
    List<Ligacao> findAllByCliente_IdOrderByCodigoAsc(UUID clienteId);
}
