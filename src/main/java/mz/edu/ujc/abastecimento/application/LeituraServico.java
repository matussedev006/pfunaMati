package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.domain.exception.ContadorNaoEncontradoException;
import mz.edu.ujc.abastecimento.domain.exception.RegraNegocioException;
import mz.edu.ujc.abastecimento.domain.model.Contador;
import mz.edu.ujc.abastecimento.domain.model.Leitura;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataContadorRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLeituraRepositorio;
import mz.edu.ujc.abastecimento.presentation.rest.dto.RegistarLeituraPedido;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LeituraServico {

    private final SpringDataContadorRepositorio contadores;
    private final SpringDataLeituraRepositorio leituras;

    public LeituraServico(
            SpringDataContadorRepositorio contadores,
            SpringDataLeituraRepositorio leituras
    ) {
        this.contadores = contadores;
        this.leituras = leituras;
    }

    @Transactional(readOnly = true)
    public List<Leitura> ultimas() {
        return leituras.findTop50ByOrderByDataLeituraDesc();
    }

    @Transactional
    public Leitura registar(RegistarLeituraPedido pedido, Utilizador utilizador) {
        LocalDateTime agora = LocalDateTime.now();
        if (pedido.dataLeitura().isAfter(agora)) {
            throw new RegraNegocioException("A data da leitura nao pode estar no futuro.");
        }

        Contador contador = contadores.findByIdAndEstado(pedido.contadorId(), "ACTIVO")
                .orElseThrow(() -> new ContadorNaoEncontradoException(pedido.contadorId()));

        leituras.findFirstByContador_IdOrderByDataLeituraDesc(contador.getId())
                .ifPresent(anterior -> {
                    if (!pedido.dataLeitura().isAfter(anterior.getDataLeitura())) {
                        throw new RegraNegocioException(
                                "A data da leitura deve ser posterior a ultima leitura registada."
                        );
                    }
                    if (pedido.valorM3().compareTo(anterior.getValorM3()) < 0) {
                        throw new RegraNegocioException(
                                "A leitura acumulada nao pode ser inferior a leitura anterior."
                        );
                    }
                });

        Leitura leitura = new Leitura(
                contador,
                utilizador,
                pedido.dataLeitura(),
                pedido.valorM3(),
                pedido.tipo(),
                pedido.observacao()
        );

        try {
            return leituras.save(leitura);
        } catch (DataIntegrityViolationException exception) {
            throw new RegraNegocioException(
                    "Ja existe uma leitura registada para este contador nesta data e hora."
            );
        }
    }
}
