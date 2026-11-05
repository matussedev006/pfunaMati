package mz.edu.ujc.abastecimento.domain.exception;

public class ClienteJaExistenteException extends RuntimeException {

    public ClienteJaExistenteException(String documento) {
        super("Ja existe um cliente registado com o documento " + documento);
    }
}
