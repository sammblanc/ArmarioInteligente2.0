package br.com.unit.tokseg.armariointeligente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.unit.tokseg.armariointeligente.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
Optional<Usuario> findByEmail(String email);
}
