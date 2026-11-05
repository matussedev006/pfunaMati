package mz.edu.ujc.abastecimento.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tarifas")
public class Tarifa {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(name = "taxa_fixa", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxaFixa;

    @Column(name = "preco_por_m3", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoPorM3;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @Column(name = "vigente_ate")
    private LocalDate vigenteAte;

    @Column(nullable = false)
    private boolean activa;

    protected Tarifa() {
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getTaxaFixa() {
        return taxaFixa;
    }

    public BigDecimal getPrecoPorM3() {
        return precoPorM3;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public boolean isActiva() {
        return activa;
    }
}
