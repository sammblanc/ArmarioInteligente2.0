package br.com.unit.tokseg.armariointeligente.repository;

import br.com.unit.tokseg.armariointeligente.model.Armario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArmarioRepository extends JpaRepository<Armario, Long> {
    List<Armario> findByCondominioId(Long condominioId);
    Optional<Armario> findByIdentificacaoAndCondominioId(String identificacao, Long condominioId);
}
