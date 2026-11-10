package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.domain.exception.ClienteJaExistenteException;
import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.domain.repository.ClienteRepositorio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClienteServicoTest {

    private final ClienteRepositorio repositorio = mock(ClienteRepositorio.class);
    private final ClienteServico servico = new ClienteServico(repositorio);

    @Test
    void registaClienteComDocumentoAindaNaoUtilizado() {
        when(repositorio.existePorDocumento("12345")).thenReturn(false);
        when(repositorio.guardar(org.mockito.ArgumentMatchers.any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cliente cliente = servico.registar("Ana Mussa", "12345", "ana@example.com", null);

        assertEquals("Ana Mussa", cliente.getNome());
        assertEquals("12345", cliente.getDocumento());
        verify(repositorio).guardar(org.mockito.ArgumentMatchers.any(Cliente.class));
    }

    @Test
    void rejeitaDocumentoJaRegistado() {
        when(repositorio.existePorDocumento("12345")).thenReturn(true);

        assertThrows(
                ClienteJaExistenteException.class,
                () -> servico.registar("Ana Mussa", "12345", "ana@example.com", null)
        );
    }


}
