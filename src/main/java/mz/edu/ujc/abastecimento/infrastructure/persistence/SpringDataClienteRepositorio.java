package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataClienteRepositorio extends JpaRepository<Cliente, UUID> {

    boolean existsByDocumento(String documento);

    List<Cliente> findDistinctByUtilizadores_Id(UUID utilizadorId);
}
