package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Consumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataConsumoRepositorio extends JpaRepository<Consumo, UUID> {

    Optional<Consumo> findFirstByContador_IdOrderByPeriodoFimDesc(UUID contadorId);
}
