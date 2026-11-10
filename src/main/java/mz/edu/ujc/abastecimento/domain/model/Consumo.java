package mz.edu.ujc.abastecimento.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consumos")
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ligacao_id", nullable = false)
    private Ligacao ligacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contador_id", nullable = false)
    private Contador contador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leitura_inicial_id", nullable = false)
    private Leitura leituraInicial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leitura_final_id", nullable = false)
    private Leitura leituraFinal;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private LocalDate periodoFim;

    @Column(name = "volume_m3", nullable = false, precision = 12, scale = 3)
    private BigDecimal volumeM3;

    @Column(name = "data_calculo", nullable = false)
    private LocalDateTime dataCalculo;

    protected Consumo() {
    }

    public Consumo(
            Ligacao ligacao,
            Contador contador,
            Leitura leituraInicial,
            Leitura leituraFinal,
            LocalDate periodoInicio,
            LocalDate periodoFim,
            BigDecimal volumeM3
    ) {
        this.ligacao = ligacao;
        this.contador = contador;
        this.leituraInicial = leituraInicial;
        this.leituraFinal = leituraFinal;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
        this.volumeM3 = volumeM3;
        this.dataCalculo = LocalDateTime.now();
    }

    @PrePersist
    private void prePersist() {
        if (dataCalculo == null) {
            dataCalculo = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Ligacao getLigacao() {
        return ligacao;
    }

    public Contador getContador() {
        return contador;
    }

    public Leitura getLeituraInicial() {
        return leituraInicial;
    }

    public Leitura getLeituraFinal() {
        return leituraFinal;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public LocalDate getPeriodoFim() {
        return periodoFim;
    }

    public BigDecimal getVolumeM3() {
        return volumeM3;
    }

    public LocalDateTime getDataCalculo() {
        return dataCalculo;
    }
}
