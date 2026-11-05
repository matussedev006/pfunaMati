package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataTarifaRepository extends JpaRepository<Tarifa, UUID> {

    List<Tarifa> findAllByOrderByNomeAsc();
}
