package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Factura;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFacturaRepositorio extends JpaRepository<Factura, UUID> {

    @EntityGraph(attributePaths = {"cliente", "ligacao", "tarifa", "consumo", "consumo.contador"})
    List<Factura> findAllByOrderByDataEmissaoDescDataCriacaoDesc();

    @EntityGraph(attributePaths = {"cliente", "ligacao", "tarifa", "consumo", "consumo.contador"})
    List<Factura> findDistinctByCliente_Utilizadores_IdOrderByDataEmissaoDescDataCriacaoDesc(UUID utilizadorId);

    @EntityGraph(attributePaths = {"cliente", "ligacao", "tarifa", "consumo", "consumo.contador"})
    Optional<Factura> findById(UUID id);

    @EntityGraph(attributePaths = {"cliente", "ligacao", "tarifa", "consumo", "consumo.contador"})
    Optional<Factura> findByNumero(String numero);

    long countByEstado(String estado);

    @Query("select coalesce(sum(f.valorTotal), 0) from Factura f")
    BigDecimal somarValorTotal();

    @Query("select coalesce(sum(f.valorTotal), 0) from Factura f where f.estado = :estado")
    BigDecimal somarValorPorEstado(@Param("estado") String estado);

    @Query("select coalesce(sum(f.consumo.volumeM3), 0) from Factura f")
    BigDecimal somarVolumeTotalM3();
}
