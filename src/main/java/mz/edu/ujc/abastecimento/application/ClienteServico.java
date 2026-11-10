package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.domain.exception.ClienteJaExistenteException;
import mz.edu.ujc.abastecimento.domain.exception.ClienteNaoEncontradoException;
import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.domain.repository.ClienteRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteServico {

    private final ClienteRepositorio clientes;

    public ClienteServico(ClienteRepositorio clientes) {
        this.clientes = clientes;
    }

    @Transactional
    public Cliente registar(String nome, String documento, String email, String telefone) {
        if (clientes.existePorDocumento(documento.trim())) {
            throw new ClienteJaExistenteException(documento);
        }
        return clientes.guardar(new Cliente(nome, documento, email, telefone));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clientes.listar();
    }

    @Transactional(readOnly = true)
    public Cliente procurarPorId(UUID id) {
        return clientes.procurarPorId(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }
}
