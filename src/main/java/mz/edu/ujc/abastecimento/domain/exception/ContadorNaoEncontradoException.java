package mz.edu.ujc.abastecimento.domain.exception;

import java.util.UUID;

public class ContadorNaoEncontradoException extends RuntimeException {

    public ContadorNaoEncontradoException(UUID id) {
        super("Contador activo nao encontrado: " + id);
    }
}
