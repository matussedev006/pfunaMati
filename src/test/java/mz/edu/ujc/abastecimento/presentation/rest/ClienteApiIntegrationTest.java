package mz.edu.ujc.abastecimento.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.domain.model.Perfil;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataClienteRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataPerfilRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataUtilizadorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClienteApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataPerfilRepository perfis;

    @Autowired
    private SpringDataClienteRepository clientes;

    @Autowired
    private SpringDataUtilizadorRepository utilizadores;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void servePaginaInicialERecursosDoFrontend() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("pfunaMati")))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("/js/app.js")));

        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));

        mockMvc.perform(get("/js/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/javascript"));
    }

    @Test
    void exigeAutenticacaoNasRotasDaApi() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LEITOR")
    void leitorConsultaClientesMasNaoGereUtilizadores() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/utilizadores"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNaoAcedeAListaGlobalDeClientesNemAOperacoesInternas() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/dashboard/resumo"))
                .andExpect(status().isForbidden());
    }

    @Test
    void iniciaSessaoComCredenciaisEEncerraAcessoAoTerminarSessao() throws Exception {
        String nomeUtilizador = "auth-" + UUID.randomUUID();
        Perfil perfilAdmin = perfis.save(new Perfil("ADMIN", "Administrador", "Acesso administrativo."));
        utilizadores.save(new Utilizador(
                "Utilizador de teste",
                nomeUtilizador,
                nomeUtilizador + "@example.test",
                passwordEncoder.encode("senha-segura-de-teste"),
                Set.of(perfilAdmin)
        ));

        MvcResult resultadoLogin = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nomeUtilizador", nomeUtilizador,
                                "senha", "senha-segura-de-teste"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeUtilizador").value(nomeUtilizador))
                .andExpect(jsonPath("$.perfis[0]").value("ADMIN"))
                .andReturn();

        MockHttpSession sessao = (MockHttpSession) resultadoLogin.getRequest().getSession(false);
        assertNotNull(sessao);

        mockMvc.perform(get("/api/v1/auth/me").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeUtilizador").value(nomeUtilizador));

        mockMvc.perform(post("/api/v1/auth/logout").session(sessao).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/me").session(sessao))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void portalDeClienteDevolveSomenteRegistosAssociados() throws Exception {
        String sufixo = UUID.randomUUID().toString();
        Perfil perfilCliente = perfis.findAllByCodigoIn(Set.of("CLIENTE")).stream()
                .findFirst()
                .orElseGet(() -> perfis.save(new Perfil("CLIENTE", "Cliente", "Acesso ao portal do cliente.")));

        Cliente clienteProprio = clientes.save(new Cliente(
                "Cliente proprio",
                "PA-" + sufixo,
                "portal.a." + sufixo + "@example.test",
                null
        ));
        Cliente outroCliente = clientes.save(new Cliente(
                "Outro cliente",
                "PB-" + sufixo,
                "portal.b." + sufixo + "@example.test",
                null
        ));

        Utilizador contaPropria = criarContaCliente("portal.a." + sufixo, clienteProprio, perfilCliente);
        criarContaCliente("portal.b." + sufixo, outroCliente, perfilCliente);

        MvcResult resultadoLogin = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nomeUtilizador", contaPropria.getUsername(),
                                "senha", "senha-segura-de-teste"
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession sessao = (MockHttpSession) resultadoLogin.getRequest().getSession(false);

        mockMvc.perform(get("/api/v1/portal/clientes").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value(clienteProprio.getNome()));

        mockMvc.perform(get("/api/v1/portal/leituras").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Utilizador criarContaCliente(String nomeUtilizador, Cliente cliente, Perfil perfilCliente) {
        Utilizador utilizador = utilizadores.save(new Utilizador(
                cliente.getNome(),
                nomeUtilizador,
                nomeUtilizador + "@example.test",
                passwordEncoder.encode("senha-segura-de-teste"),
                Set.of(perfilCliente)
        ));
        cliente.associarUtilizador(utilizador);
        clientes.save(cliente);
        return utilizador;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registaConsultaERejeitaClienteDuplicado() throws Exception {
        String documento = "IT-" + UUID.randomUUID();
        String pedido = """
                {
                  "nome": "Ana Mussa",
                  "documento": "%s",
                  "email": "ana@example.com",
                  "telefone": "+258 84 000 0000"
                }
                """.formatted(documento);

        MvcResult resultado = mockMvc.perform(post("/api/v1/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documento").value(documento))
                .andReturn();

        String id = objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(get("/api/v1/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Mussa"));

        mockMvc.perform(post("/api/v1/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejeitaPedidoComEmailInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Mussa",
                                  "documento": "INVALIDO-%s",
                                  "email": "nao-e-email",
                                  "telefone": ""
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
