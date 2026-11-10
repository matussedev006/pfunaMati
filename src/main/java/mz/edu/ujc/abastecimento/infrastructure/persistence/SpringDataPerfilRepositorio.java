package mz.edu.ujc.abastecimento.infrastructure.persistence;

import mz.edu.ujc.abastecimento.domain.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataPerfilRepositorio extends JpaRepository<Perfil, Long> {

    List<Perfil> findAllByCodigoIn(Collection<String> codigos);
}
