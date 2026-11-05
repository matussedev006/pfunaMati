package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.domain.exception.ClienteJaExistenteException;
import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.domain.repository.ClienteRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClienteServiceTest {

    private final ClienteRepository repository = mock(ClienteRepository.class);
    private final ClienteService service = new ClienteService(repository);

    @Test
    void registaClienteComDocumentoAindaNaoUtilizado() {
        when(repository.existePorDocumento("12345")).thenReturn(false);
        when(repository.guardar(org.mockito.ArgumentMatchers.any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cliente cliente = service.registar("Ana Mussa", "12345", "ana@example.com", null);

        assertEquals("Ana Mussa", cliente.getNome());
        assertEquals("12345", cliente.getDocumento());
        verify(repository).guardar(org.mockito.ArgumentMatchers.any(Cliente.class));
    }

    @Test
    void rejeitaDocumentoJaRegistado() {
        when(repository.existePorDocumento("12345")).thenReturn(true);

        assertThrows(
                ClienteJaExistenteException.class,
                () -> service.registar("Ana Mussa", "12345", "ana@example.com", null)
        );
    }
}
