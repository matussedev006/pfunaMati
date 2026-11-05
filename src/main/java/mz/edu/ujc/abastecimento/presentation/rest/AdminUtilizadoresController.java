package mz.edu.ujc.abastecimento.presentation.rest;

import jakarta.validation.Valid;
import mz.edu.ujc.abastecimento.application.UtilizadorAdminService;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.presentation.rest.dto.AdminUtilizadorResponse;
import mz.edu.ujc.abastecimento.presentation.rest.dto.CriarUtilizadorRequest;
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
public class AdminUtilizadoresController {

    private final UtilizadorAdminService utilizadores;

    public AdminUtilizadoresController(UtilizadorAdminService utilizadores) {
        this.utilizadores = utilizadores;
    }

    @GetMapping
    public List<AdminUtilizadorResponse> listar() {
        return utilizadores.listar().stream().map(AdminUtilizadorResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUtilizadorResponse criar(@Valid @RequestBody CriarUtilizadorRequest pedido) {
        Utilizador utilizador = utilizadores.criar(pedido);
        return AdminUtilizadorResponse.de(utilizador);
    }
}
