package mz.edu.ujc.abastecimento.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ligacao_id", nullable = false)
    private Ligacao ligacao;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consumo_id", nullable = false, unique = true)
    private Consumo consumo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarifa_id", nullable = false)
    private Tarifa tarifa;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private LocalDate periodoFim;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "valor_consumo", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorConsumo;

    @Column(name = "taxa_fixa", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxaFixa;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    protected Factura() {
    }

    public Factura(
            String numero,
            Cliente cliente,
            Ligacao ligacao,
            Consumo consumo,
            Tarifa tarifa,
            LocalDate periodoInicio,
            LocalDate periodoFim,
            LocalDate dataEmissao,
            LocalDate dataVencimento,
            BigDecimal valorConsumo,
            BigDecimal taxaFixa,
            BigDecimal valorTotal,
            String estado
    ) {
        this.numero = numero;
        this.cliente = cliente;
        this.ligacao = ligacao;
        this.consumo = consumo;
        this.tarifa = tarifa;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.valorConsumo = valorConsumo;
        this.taxaFixa = taxaFixa;
        this.valorTotal = valorTotal;
        this.estado = estado != null ? estado : "PENDENTE";
        this.dataCriacao = LocalDateTime.now();
    }

    @PrePersist
    private void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PENDENTE";
        }
    }

    public void marcarComoPaga() {
        this.estado = "PAGA";
    }

    public void anular() {
        this.estado = "ANULADA";
    }

    public UUID getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Ligacao getLigacao() {
        return ligacao;
    }

    public Consumo getConsumo() {
        return consumo;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public LocalDate getPeriodoFim() {
        return periodoFim;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public BigDecimal getValorConsumo() {
        return valorConsumo;
    }

    public BigDecimal getTaxaFixa() {
        return taxaFixa;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}
