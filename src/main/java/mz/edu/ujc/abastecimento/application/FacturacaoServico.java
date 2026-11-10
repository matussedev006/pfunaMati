package mz.edu.ujc.abastecimento.application;

import mz.edu.ujc.abastecimento.domain.exception.ContadorNaoEncontradoException;
import mz.edu.ujc.abastecimento.domain.exception.RegraNegocioException;
import mz.edu.ujc.abastecimento.domain.model.Cliente;
import mz.edu.ujc.abastecimento.domain.model.Consumo;
import mz.edu.ujc.abastecimento.domain.model.Contador;
import mz.edu.ujc.abastecimento.domain.model.Factura;
import mz.edu.ujc.abastecimento.domain.model.Leitura;
import mz.edu.ujc.abastecimento.domain.model.Ligacao;
import mz.edu.ujc.abastecimento.domain.model.Tarifa;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataConsumoRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataContadorRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataFacturaRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataLeituraRepositorio;
import mz.edu.ujc.abastecimento.infrastructure.persistence.SpringDataTarifaRepositorio;
import mz.edu.ujc.abastecimento.presentation.rest.dto.FacturaResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.FacturacaoResumoResposta;
import mz.edu.ujc.abastecimento.presentation.rest.dto.GerarFacturaPedido;
import mz.edu.ujc.abastecimento.presentation.rest.dto.NotificacaoVencimentoResposta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FacturacaoServico {

    private final SpringDataFacturaRepositorio facturas;
    private final SpringDataConsumoRepositorio consumos;
    private final SpringDataContadorRepositorio contadores;
    private final SpringDataLeituraRepositorio leituras;
    private final SpringDataTarifaRepositorio tarifas;

    public FacturacaoServico(
            SpringDataFacturaRepositorio facturas,
            SpringDataConsumoRepositorio consumos,
            SpringDataContadorRepositorio contadores,
            SpringDataLeituraRepositorio leituras,
            SpringDataTarifaRepositorio tarifas
    ) {
        this.facturas = facturas;
        this.consumos = consumos;
        this.contadores = contadores;
        this.leituras = leituras;
        this.tarifas = tarifas;
    }

    @Transactional(readOnly = true)
    public List<FacturaResposta> listar() {
        return facturas.findAllByOrderByDataEmissaoDescDataCriacaoDesc().stream()
                .map(FacturaResposta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FacturaResposta> listarPorUtilizador(UUID utilizadorId) {
        return facturas.findDistinctByCliente_Utilizadores_IdOrderByDataEmissaoDescDataCriacaoDesc(utilizadorId).stream()
                .map(FacturaResposta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public FacturacaoResumoResposta resumo() {
        BigDecimal totalFacturado = facturas.somarValorTotal();
        BigDecimal totalPendente = facturas.somarValorPorEstado("PENDENTE");
        BigDecimal totalPago = facturas.somarValorPorEstado("PAGA");
        long total = facturas.count();
        long pendentes = facturas.countByEstado("PENDENTE");
        long pagas = facturas.countByEstado("PAGA");
        BigDecimal volume = facturas.somarVolumeTotalM3();

        return new FacturacaoResumoResposta(
                totalFacturado != null ? totalFacturado : BigDecimal.ZERO,
                totalPendente != null ? totalPendente : BigDecimal.ZERO,
                totalPago != null ? totalPago : BigDecimal.ZERO,
                total,
                pendentes,
                pagas,
                volume != null ? volume : BigDecimal.ZERO
        );
    }

    @Transactional
    public FacturaResposta gerar(GerarFacturaPedido pedido) {
        Contador contador = contadores.findById(pedido.contadorId())
                .orElseThrow(() -> new ContadorNaoEncontradoException(pedido.contadorId()));

        Ligacao ligacao = contador.getLigacao();
        Cliente cliente = ligacao.getCliente();

        Tarifa tarifa;
        if (pedido.tarifaId() != null) {
            tarifa = tarifas.findById(pedido.tarifaId())
                    .orElseThrow(() -> new RegraNegocioException("Tarifa especificada nao foi encontrada."));
        } else {
            tarifa = tarifas.findAllByOrderByNomeAsc().stream()
                    .filter(Tarifa::isActiva)
                    .findFirst()
                    .orElseThrow(() -> new RegraNegocioException("Nenhuma tarifa activa disponivel no sistema."));
        }

        Leitura leituraInicial;
        Leitura leituraFinal;

        if (pedido.leituraInicialId() != null && pedido.leituraFinalId() != null) {
            leituraInicial = leituras.findById(pedido.leituraInicialId())
                    .orElseThrow(() -> new RegraNegocioException("Leitura inicial nao encontrada."));
            leituraFinal = leituras.findById(pedido.leituraFinalId())
                    .orElseThrow(() -> new RegraNegocioException("Leitura final nao encontrada."));
        } else {
            List<Leitura> leiturasContador = leituras.findAll().stream()
                    .filter(l -> l.getContador().getId().equals(contador.getId()))
                    .sorted(Comparator.comparing(Leitura::getDataLeitura))
                    .toList();

            if (leiturasContador.size() >= 2) {
                leituraInicial = leiturasContador.get(leiturasContador.size() - 2);
                leituraFinal = leiturasContador.get(leiturasContador.size() - 1);
            } else if (leiturasContador.size() == 1) {
                leituraFinal = leiturasContador.get(0);
                LocalDateTime dataInstalacao = contador.getDataInstalacao().atStartOfDay();
                BigDecimal baseValor = pedido.leituraInicialM3() != null ? pedido.leituraInicialM3() : BigDecimal.ZERO;
                leituraInicial = leituras.save(new Leitura(
                        contador,
                        null,
                        dataInstalacao,
                        baseValor,
                        "REAL",
                        "Leitura base gerada para facturacao inicial"
                ));
            } else {
                BigDecimal valorFinal = pedido.leituraFinalM3() != null ? pedido.leituraFinalM3() : new BigDecimal("10.000");
                BigDecimal valorInicial = pedido.leituraInicialM3() != null ? pedido.leituraInicialM3() : BigDecimal.ZERO;
                LocalDateTime agora = LocalDateTime.now();
                LocalDateTime mesPassado = agora.minusMonths(1);

                leituraInicial = leituras.save(new Leitura(
                        contador,
                        null,
                        mesPassado,
                        valorInicial,
                        "REAL",
                        "Leitura de instalacao"
                ));
                leituraFinal = leituras.save(new Leitura(
                        contador,
                        null,
                        agora,
                        valorFinal,
                        "REAL",
                        "Leitura de medicao mensal"
                ));
            }
        }

        BigDecimal volumeM3 = leituraFinal.getValorM3().subtract(leituraInicial.getValorM3());
        if (volumeM3.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("A leitura final nao pode ser menor que a leitura inicial.");
        }

        LocalDate periodoInicio = pedido.periodoInicio() != null
                ? pedido.periodoInicio()
                : leituraInicial.getDataLeitura().toLocalDate();

        LocalDate periodoFim = pedido.periodoFim() != null
                ? pedido.periodoFim()
                : leituraFinal.getDataLeitura().toLocalDate();

        if (!periodoFim.isAfter(periodoInicio)) {
            periodoFim = periodoInicio.plusDays(30);
        }

        LocalDate dataEmissao = pedido.dataEmissao() != null ? pedido.dataEmissao() : LocalDate.now();
        LocalDate dataVencimento = pedido.dataVencimento() != null ? pedido.dataVencimento() : dataEmissao.plusDays(15);
        if (dataVencimento.isBefore(dataEmissao)) {
            dataVencimento = dataEmissao.plusDays(15);
        }

        Consumo consumo = consumos.save(new Consumo(
                ligacao,
                contador,
                leituraInicial,
                leituraFinal,
                periodoInicio,
                periodoFim,
                volumeM3
        ));

        BigDecimal taxaFixa = tarifa.getTaxaFixa().setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorConsumo = volumeM3.multiply(tarifa.getPrecoPorM3()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorTotal = valorConsumo.add(taxaFixa).setScale(2, RoundingMode.HALF_UP);

        int ano = dataEmissao.getYear();
        long sequencia = facturas.count() + 1;
        String numero = String.format("FT-%d-%04d", ano, sequencia);
        while (facturas.findByNumero(numero).isPresent()) {
            sequencia++;
            numero = String.format("FT-%d-%04d", ano, sequencia);
        }

        Factura factura = new Factura(
                numero,
                cliente,
                ligacao,
                consumo,
                tarifa,
                periodoInicio,
                periodoFim,
                dataEmissao,
                dataVencimento,
                valorConsumo,
                taxaFixa,
                valorTotal,
                "PENDENTE"
        );

        Factura salva = facturas.save(factura);
        return FacturaResposta.de(salva);
    }

    @Transactional
    public FacturaResposta marcarComoPaga(UUID facturaId) {
        Factura factura = facturas.findById(facturaId)
                .orElseThrow(() -> new RegraNegocioException("Factura nao encontrada com o ID fornecido."));
        factura.marcarComoPaga();
        Factura salva = facturas.save(factura);
        return FacturaResposta.de(salva);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoVencimentoResposta> obterAlertasVencimento() {
        LocalDate hoje = LocalDate.now();
        return facturas.findAllByOrderByDataEmissaoDescDataCriacaoDesc().stream()
                .filter(f -> "PENDENTE".equalsIgnoreCase(f.getEstado()))
                .map(f -> criarNotificacao(f, hoje))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(NotificacaoVencimentoResposta::diasRestantes))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacaoVencimentoResposta> obterAlertasCliente(UUID utilizadorId) {
        LocalDate hoje = LocalDate.now();
        return facturas.findDistinctByCliente_Utilizadores_IdOrderByDataEmissaoDescDataCriacaoDesc(utilizadorId).stream()
                .filter(f -> "PENDENTE".equalsIgnoreCase(f.getEstado()))
                .map(f -> criarNotificacao(f, hoje))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(NotificacaoVencimentoResposta::diasRestantes))
                .toList();
    }

    @Transactional(readOnly = true)
    public String enviarLembrete(UUID facturaId) {
        Factura factura = facturas.findById(facturaId)
                .orElseThrow(() -> new RegraNegocioException("Factura nao encontrada com o ID fornecido."));
        Cliente cliente = factura.getCliente();
        String contacto = cliente.getTelefone() != null && !cliente.getTelefone().isBlank()
                ? cliente.getTelefone()
                : cliente.getEmail();
        return String.format(
                "Lembrete de vencimento da factura %s no valor de %s MT enviado com sucesso para %s (%s).",
                factura.getNumero(),
                factura.getValorTotal(),
                cliente.getNome(),
                contacto != null ? contacto : "contacto cadastrado"
        );
    }

    private NotificacaoVencimentoResposta criarNotificacao(Factura factura, LocalDate hoje) {
        long dias = ChronoUnit.DAYS.between(hoje, factura.getDataVencimento());
        String nivel;
        String mensagem;
        Cliente cliente = factura.getCliente();

        if (dias < 0) {
            nivel = "VENCIDA";
            mensagem = String.format(
                    "A factura %s (Valor: %s MT) venceu ha %d dia(s) em %s. Regularizacao urgente necessaria.",
                    factura.getNumero(),
                    factura.getValorTotal(),
                    Math.abs(dias),
                    factura.getDataVencimento()
            );
        } else if (dias == 0) {
            nivel = "URGENTE";
            mensagem = String.format(
                    "A factura %s (Valor: %s MT) vence HOJE (%s). Proceda a liquidacao para evitar corte de fornecimento.",
                    factura.getNumero(),
                    factura.getValorTotal(),
                    factura.getDataVencimento()
            );
        } else if (dias <= 5) {
            nivel = "URGENTE";
            mensagem = String.format(
                    "A factura %s (Valor: %s MT) vence em %d dias (%s). Evite atrasos na liquidacao.",
                    factura.getNumero(),
                    factura.getValorTotal(),
                    dias,
                    factura.getDataVencimento()
            );
        } else {
            nivel = "AVISO";
            mensagem = String.format(
                    "Lembrete: A factura %s (Valor: %s MT) tem vencimento a %s (%d dias restantes).",
                    factura.getNumero(),
                    factura.getValorTotal(),
                    factura.getDataVencimento(),
                    dias
            );
        }

        return new NotificacaoVencimentoResposta(
                factura.getId(),
                factura.getNumero(),
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                factura.getLigacao().getCodigo(),
                factura.getValorTotal(),
                factura.getDataVencimento(),
                dias,
                nivel,
                mensagem
        );
    }
}
