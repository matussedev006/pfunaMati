package mz.edu.ujc.abastecimento.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contadores")
public class Contador {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ligacao_id", nullable = false)
    private Ligacao ligacao;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 60)
    private String numeroSerie;

    @Column(length = 100)
    private String modelo;

    @Column(name = "data_instalacao", nullable = false)
    private LocalDate dataInstalacao;

    @Column(name = "data_remocao")
    private LocalDate dataRemocao;

    @Column(nullable = false, length = 20)
    private String estado;

    protected Contador() {
    }

    public UUID getId() {
        return id;
    }

    public Ligacao getLigacao() {
        return ligacao;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public String getModelo() {
        return modelo;
    }

    public LocalDate getDataInstalacao() {
        return dataInstalacao;
    }

    public String getEstado() {
        return estado;
    }
}
