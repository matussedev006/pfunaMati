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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leituras")
public class Leitura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contador_id", nullable = false)
    private Contador contador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    private Utilizador utilizador;

    @Column(name = "data_leitura", nullable = false)
    private LocalDateTime dataLeitura;

    @Column(name = "valor_m3", nullable = false, precision = 12, scale = 3)
    private BigDecimal valorM3;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(length = 500)
    private String observacao;

    @Column(name = "data_registo", nullable = false, insertable = false, updatable = false)
    private Instant dataRegisto;

    protected Leitura() {
    }

    public Leitura(
            Contador contador,
            Utilizador utilizador,
            LocalDateTime dataLeitura,
            BigDecimal valorM3,
            String tipo,
            String observacao
    ) {
        this.contador = contador;
        this.utilizador = utilizador;
        this.dataLeitura = dataLeitura;
        this.valorM3 = valorM3;
        this.tipo = tipo;
        this.observacao = observacao;
    }

    @PrePersist
    private void definirDataRegisto() {
        if (dataRegisto == null) {
            dataRegisto = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Contador getContador() {
        return contador;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    public LocalDateTime getDataLeitura() {
        return dataLeitura;
    }

    public BigDecimal getValorM3() {
        return valorM3;
    }

    public String getTipo() {
        return tipo;
    }

    public String getObservacao() {
        return observacao;
    }
}
