package mz.edu.ujc.abastecimento.presentation.rest;

import mz.edu.ujc.abastecimento.domain.exception.ClienteJaExistenteException;
import mz.edu.ujc.abastecimento.domain.exception.ClienteNaoEncontradoException;
import mz.edu.ujc.abastecimento.domain.exception.ContadorNaoEncontradoException;
import mz.edu.ujc.abastecimento.domain.exception.RegraNegocioException;
import mz.edu.ujc.abastecimento.presentation.rest.dto.ErroApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManipuladorExcecoesApi {

    @ExceptionHandler(ClienteJaExistenteException.class)
    public ResponseEntity<ErroApi> tratarDuplicado(ClienteJaExistenteException exception) {
        return erro(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErroApi> tratarNaoEncontrado(ClienteNaoEncontradoException exception) {
        return erro(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ContadorNaoEncontradoException.class)
    public ResponseEntity<ErroApi> tratarContadorNaoEncontrado(ContadorNaoEncontradoException exception) {
        return erro(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroApi> tratarRegraNegocio(RegraNegocioException exception) {
        return erro(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApi> tratarValidacao(MethodArgumentNotValidException exception) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseEntity<ErroApi> erro(HttpStatus estado, String mensagem) {
        ErroApi resposta = new ErroApi(
                Instant.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensagem
        );
        return ResponseEntity.status(estado).body(resposta);
    }
}
