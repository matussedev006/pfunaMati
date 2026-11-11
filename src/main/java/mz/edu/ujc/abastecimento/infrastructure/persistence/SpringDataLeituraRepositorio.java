package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Leitura;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataLeituraRepositorio extends JpaRepository<Leitura, UUID> {

    @EntityGraph(attributePaths = {"contador", "contador.ligacao", "contador.ligacao.cliente", "utilizador"})
    List<Leitura> findTop50ByOrderByDataLeituraDesc();

    @EntityGraph(attributePaths = {"contador", "contador.ligacao", "contador.ligacao.cliente", "utilizador"})
    Optional<Leitura> findFirstByContador_IdOrderByDataLeituraDesc(UUID contadorId);

    @EntityGraph(attributePaths = {"contador", "contador.ligacao", "contador.ligacao.cliente", "utilizador"})
    List<Leitura> findTop50DistinctByContador_Ligacao_Cliente_Utilizadores_IdOrderByDataLeituraDesc(
            UUID utilizadorId
    );

    @Query("""
            select count(l) from Leitura l
            where l.dataLeitura >= :inicio and l.dataLeitura < :fim
            """)
    long countByDataLeituraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query(value = """
            select coalesce(sum(m.delta_m3), 0)
            from (
                select max(valor_m3) - min(valor_m3) as delta_m3
                from leituras
                where data_leitura >= :inicio and data_leitura < :fim
                group by contador_id
                having count(*) >= 2
            ) m
            """, nativeQuery = true)
    BigDecimal sumValorM3ByDataLeituraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
