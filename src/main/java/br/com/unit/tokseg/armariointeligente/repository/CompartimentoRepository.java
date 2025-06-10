package br.com.unit.tokseg.armariointeligente.repository;

import br.com.unit.tokseg.armariointeligente.model.Compartimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompartimentoRepository extends JpaRepository<Compartimento, Long> {
    List<Compartimento> findByArmarioId(Long armarioId);
    List<Compartimento> findByOcupado(Boolean ocupado);
    Optional<Compartimento> findByNumeroAndArmarioId(String numero, Long armarioId);
}
