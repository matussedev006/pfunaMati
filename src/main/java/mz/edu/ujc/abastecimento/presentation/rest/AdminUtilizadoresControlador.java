package mz.edu.ujc.abastecimento.presentation.rest;

import jakarta.validation.Valid;
import mz.edu.ujc.abastecimento.application.UtilizadorAdminServico;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.presentation.rest.dto.AdminUtilizadorResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.CriarUtilizadorPedido;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/utilizadores")
public class AdminUtilizadoresControlador {

    private final UtilizadorAdminServico utilizadores;

    public AdminUtilizadoresControlador(UtilizadorAdminServico utilizadores) {
        this.utilizadores = utilizadores;
    }

    @GetMapping
    public List<AdminUtilizadorResposta> listar() {
        return utilizadores.listar().stream().map(AdminUtilizadorResposta::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUtilizadorResposta criar(@Valid @RequestBody CriarUtilizadorPedido pedido) {
        Utilizador utilizador = utilizadores.criar(pedido);
        return AdminUtilizadorResposta.de(utilizador);
    }
}
