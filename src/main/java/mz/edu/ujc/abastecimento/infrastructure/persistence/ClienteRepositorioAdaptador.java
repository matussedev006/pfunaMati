package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.domain.repository.ClienteRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ClienteRepositorioAdaptador implements ClienteRepositorio {

    private final SpringDataClienteRepositorio repositorio;

    public ClienteRepositorioAdaptador(SpringDataClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        return repositorio.save(cliente);
    }

    @Override
    public List<Cliente> listar() {
        return repositorio.findAll();
    }

    @Override
    public Optional<Cliente> procurarPorId(UUID id) {
        return repositorio.findById(id);
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return repositorio.existsByDocumento(documento);
    }
}
