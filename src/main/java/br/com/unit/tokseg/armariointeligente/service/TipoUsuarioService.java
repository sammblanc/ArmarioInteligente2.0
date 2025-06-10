package br.com.unit.tokseg.armariointeligente.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.RelatedResourceException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceAlreadyExistsException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.TipoUsuario;
import br.com.unit.tokseg.armariointeligente.repository.TipoUsuarioRepository;
import jakarta.transaction.Transactional;

@Service
public class TipoUsuarioService {
    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Transactional
    public TipoUsuario criarTipoUsuario(TipoUsuario tipoUsuario) {
        if (tipoUsuario == null) {
            throw new BadRequestException("O tipo de usuário não pode ser nulo");
        }
        if (tipoUsuario.getNome() == null || tipoUsuario.getNome().isEmpty()) {
            throw new BadRequestException("O nome do tipo de usuário não pode ser nulo ou vazio");
        }

        tipoUsuarioRepository.findByNome(tipoUsuario.getNome()).ifPresent(t -> {
            throw new ResourceAlreadyExistsException("Tipo de usuário", "nome",
                    tipoUsuario.getNome());
        });
        return tipoUsuarioRepository.save(tipoUsuario);
    }

    @Transactional
    public List<TipoUsuario> listarTiposUsuarios() {
        return tipoUsuarioRepository.findAll();
    }

    @Transactional
    public Optional<TipoUsuario> buscarTipoUsuarioPorId(Long id) {
        return tipoUsuarioRepository.findById(id);
    }

    @Transactional
    public TipoUsuario atualizarTipoUsuario(Long id, TipoUsuario tipoUsuario) {
        TipoUsuario tipoExistente = tipoUsuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de usuário", "id", id));

        // Verifica se o novo nome já existe para outro tipo de usuário
        if (tipoUsuario.getNome() != null && !tipoUsuario.getNome().isEmpty()) {
            Optional<TipoUsuario> tipoComMesmoNome =
                    tipoUsuarioRepository.findByNome(tipoUsuario.getNome());
            if (tipoComMesmoNome.isPresent() && !tipoComMesmoNome.get().getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Tipo de usuário", "nome",
                        tipoUsuario.getNome());
            }
            tipoExistente.setNome(tipoUsuario.getNome());
        }

        if (tipoUsuario.getDescricao() != null) {
            tipoExistente.setDescricao(tipoUsuario.getDescricao());
        }

        return tipoUsuarioRepository.save(tipoExistente);
    }

    @Transactional
    public void deletarTipoUsuario(Long id) {
        TipoUsuario tipoUsuario = tipoUsuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de usuário", "id", id));

        if (!tipoUsuario.getUsuarios().isEmpty()) {
            throw new RelatedResourceException("tipo de usuário", "usuários");
        }

        tipoUsuarioRepository.deleteById(id);
    }
}
