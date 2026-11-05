package mz.edu.ujc.abastecimento.domain.exception;

import java.util.UUID;

public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(UUID id) {
        super("Cliente nao encontrado: " + id);
    }
}
