package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.domain.exception.ContadorNaoEncontradoException;
import mz.edu.ujc.abastecimento.domain.exception.RegraNegocioException;
import mz.edu.ujc.abastecimento.domain.model.Contador;
import mz.edu.ujc.abastecimento.domain.model.Leitura;
import mz.edu.ujc.abastecimento.domain.model.Utilizador;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataContadorRepository;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLeituraRepository;
import mz.edu.ujc.abastecimento.presentation.rest.dto.RegistarLeituraRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LeituraService {

    private final SpringDataContadorRepository contadores;
    private final SpringDataLeituraRepository leituras;

    public LeituraService(
            SpringDataContadorRepository contadores,
            SpringDataLeituraRepository leituras
    ) {
        this.contadores = contadores;
        this.leituras = leituras;
    }

    @Transactional(readOnly = true)
    public List<Leitura> ultimas() {
        return leituras.findTop50ByOrderByDataLeituraDesc();
    }

    @Transactional
    public Leitura registar(RegistarLeituraRequest pedido, Utilizador utilizador) {
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
                pedido.observacao() == null ? null : pedido.observacao().trim()
        );

        try {
            return leituras.saveAndFlush(leitura);
        } catch (DataIntegrityViolationException exception) {
            throw new RegraNegocioException(
                    "Ja existe uma leitura para este contador nessa data."
            );
        }
    }
}
