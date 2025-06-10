package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.RelatedResourceException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceAlreadyExistsException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Armario;
import br.com.unit.tokseg.armariointeligente.model.Condominio;
import br.com.unit.tokseg.armariointeligente.repository.ArmarioRepository;
import br.com.unit.tokseg.armariointeligente.repository.CondominioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArmarioService {

    @Autowired
    private ArmarioRepository armarioRepository;

    @Autowired
    private CondominioRepository condominioRepository;

    @Transactional
    public Armario criarArmario(Armario armario) {
        if (armario == null) {
            throw new BadRequestException("Armário não pode ser nulo");
        }
        if (armario.getIdentificacao() == null || armario.getIdentificacao().isEmpty()) {
            throw new BadRequestException("Identificação do armário não pode ser nula ou vazia");
        }
        if (armario.getCondominio() == null || armario.getCondominio().getId() == null) {
            throw new BadRequestException("Condomínio é obrigatório");
        }

        Condominio condominio = condominioRepository.findById(armario.getCondominio().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Condomínio", "id", armario.getCondominio().getId()));

        armarioRepository.findByIdentificacaoAndCondominioId(armario.getIdentificacao(), condominio.getId())
                .ifPresent(a -> {
                    throw new ResourceAlreadyExistsException("Armário", "identificação", armario.getIdentificacao());
                });

        armario.setCondominio(condominio);
        return armarioRepository.save(armario);
    }

    @Transactional
    public List<Armario> listarArmarios() {
        return armarioRepository.findAll();
    }

    @Transactional
    public List<Armario> listarArmariosPorCondominio(Long condominioId) {
        if (!condominioRepository.existsById(condominioId)) {
            throw new ResourceNotFoundException("Condomínio", "id", condominioId);
        }
        return armarioRepository.findByCondominioId(condominioId);
    }

    @Transactional
    public Optional<Armario> buscarArmarioPorId(Long id) {
        return armarioRepository.findById(id);
    }

    @Transactional
    public Armario atualizarArmario(Long id, Armario armario) {
        Armario armarioExistente = armarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Armário", "id", id));

        // Verifica se a nova identificação já existe para outro armário no mesmo condomínio
        if (armario.getIdentificacao() != null && !armario.getIdentificacao().isEmpty()) {
            Long condominioId = armario.getCondominio() != null && armario.getCondominio().getId() != null ?
                    armario.getCondominio().getId() : armarioExistente.getCondominio().getId();

            Optional<Armario> armarioComMesmaIdentificacao = armarioRepository
                    .findByIdentificacaoAndCondominioId(armario.getIdentificacao(), condominioId);

            if (armarioComMesmaIdentificacao.isPresent() && !armarioComMesmaIdentificacao.get().getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Armário", "identificação", armario.getIdentificacao());
            }
            armarioExistente.setIdentificacao(armario.getIdentificacao());
        }

        // Atualiza o condomínio se fornecido
        if (armario.getCondominio() != null && armario.getCondominio().getId() != null) {
            Condominio condominio = condominioRepository.findById(armario.getCondominio().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Condomínio", "id", armario.getCondominio().getId()));
            armarioExistente.setCondominio(condominio);
        }

        if (armario.getLocalizacao() != null) {
            armarioExistente.setLocalizacao(armario.getLocalizacao());
        }

        if (armario.getDescricao() != null) {
            armarioExistente.setDescricao(armario.getDescricao());
        }

        if (armario.getAtivo() != null) {
            armarioExistente.setAtivo(armario.getAtivo());
        }

        return armarioRepository.save(armarioExistente);
    }

    @Transactional
    public void deletarArmario(Long id) {
        Armario armario = armarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Armário", "id", id));

        if (armario.getCompartimentos() != null && !armario.getCompartimentos().isEmpty()) {
            throw new RelatedResourceException("armário", "compartimentos");
        }

        armarioRepository.deleteById(id);
    }
}
