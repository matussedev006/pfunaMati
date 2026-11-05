package mz.edu.ujc.abastecimento.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.UUID;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 40)
    private String documento;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 30)
    private String telefone;

    @Column(nullable = false, updatable = false)
    private Instant dataRegisto;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "cliente_utilizadores",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "utilizador_id")
    )
    private Set<Utilizador> utilizadores = new LinkedHashSet<>();

    protected Cliente() {
    }

    public Cliente(String nome, String documento, String email, String telefone) {
        this.nome = exigirTexto(nome, "nome");
        this.documento = exigirTexto(documento, "documento");
        this.email = exigirTexto(email, "email");
        this.telefone = telefone == null ? null : telefone.trim();
    }

    @PrePersist
    private void definirDataRegisto() {
        if (dataRegisto == null) {
            dataRegisto = Instant.now();
        }
    }

    private static String exigirTexto(String valor, String campo) {
        Objects.requireNonNull(valor, campo + " nao pode ser nulo");
        String normalizado = valor.trim();
        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException(campo + " nao pode estar vazio");
        }
        return normalizado;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDocumento() {
        return documento;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public Instant getDataRegisto() {
        return dataRegisto;
    }

    public Set<Utilizador> getUtilizadores() {
        return Set.copyOf(utilizadores);
    }

    public void associarUtilizador(Utilizador utilizador) {
        utilizadores.add(Objects.requireNonNull(utilizador));
    }
}
