package mz.edu.ujc.abastecimento.domain.repository;

import mz.edu.ujc.abastecimento.domain.model.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {

    Cliente guardar(Cliente cliente);

    List<Cliente> listar();

    Optional<Cliente> procurarPorId(UUID id);

    boolean existePorDocumento(String documento);
}
