package mz.edu.ujc.abastecimento.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;

@Entity
@Table(name = "utilizadores")
public class Utilizador implements UserDetails {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "nome_utilizador", nullable = false, unique = true, length = 60)
    private String nomeUtilizador;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "utilizador_perfis",
            joinColumns = @JoinColumn(name = "utilizador_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfis = new LinkedHashSet<>();

    protected Utilizador() {
    }

    public Utilizador(
            String nome,
            String nomeUtilizador,
            String email,
            String senhaHash,
            Set<Perfil> perfis
    ) {
        this.id = UUID.randomUUID();
        this.nome = Objects.requireNonNull(nome);
        this.nomeUtilizador = Objects.requireNonNull(nomeUtilizador);
        this.email = Objects.requireNonNull(email);
        this.senhaHash = Objects.requireNonNull(senhaHash);
        this.ativo = true;
        this.dataCriacao = Instant.now();
        this.perfis = new LinkedHashSet<>(perfis);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getCodigosPerfis() {
        return perfis.stream().map(Perfil::getCodigo).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public boolean temPerfil(String codigo) {
        return perfis.stream().anyMatch(perfil -> perfil.getCodigo().equals(codigo));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfis.stream()
                .map(Perfil::getCodigo)
                .map(codigo -> new SimpleGrantedAuthority("ROLE_" + codigo))
                .toList();
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return nomeUtilizador;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
