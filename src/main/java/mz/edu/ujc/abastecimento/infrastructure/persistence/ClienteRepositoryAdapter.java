package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.domain.repository.ClienteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final SpringDataClienteRepository repository;

    public ClienteRepositoryAdapter(SpringDataClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        return repository.save(cliente);
    }

    @Override
    public List<Cliente> listar() {
        return repository.findAll();
    }

    @Override
    public Optional<Cliente> procurarPorId(UUID id) {
        return repository.findById(id);
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return repository.existsByDocumento(documento);
    }
}
