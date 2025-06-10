package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.RelatedResourceException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceAlreadyExistsException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Condominio;
import br.com.unit.tokseg.armariointeligente.repository.CondominioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CondominioService {

    @Autowired
    private CondominioRepository condominioRepository;

    @Transactional
    public Condominio criarCondominio(Condominio condominio) {
        if (condominio == null) {
            throw new BadRequestException("Condomínio não pode ser nulo");
        }
        if (condominio.getNome() == null || condominio.getNome().isEmpty()) {
            throw new BadRequestException("Nome do condomínio não pode ser nulo ou vazio");
        }
        if (condominio.getEndereco() == null || condominio.getEndereco().isEmpty()) {
            throw new BadRequestException("Endereço do condomínio não pode ser nulo ou vazio");
        }

        condominioRepository.findByNome(condominio.getNome()).ifPresent(c -> {
            throw new ResourceAlreadyExistsException("Condomínio", "nome", condominio.getNome());
        });

        return condominioRepository.save(condominio);
    }

    @Transactional
    public List<Condominio> listarCondominios() {
        return condominioRepository.findAll();
    }

    @Transactional
    public Optional<Condominio> buscarCondominioPorId(Long id) {
        return condominioRepository.findById(id);
    }

    @Transactional
    public Condominio atualizarCondominio(Long id, Condominio condominio) {
        Condominio condominioExistente = condominioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condomínio", "id", id));

        // Verifica se o novo nome já existe para outro condomínio
        if (condominio.getNome() != null && !condominio.getNome().isEmpty()) {
            Optional<Condominio> condominioComMesmoNome = condominioRepository.findByNome(condominio.getNome());
            if (condominioComMesmoNome.isPresent() && !condominioComMesmoNome.get().getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Condomínio", "nome", condominio.getNome());
            }
            condominioExistente.setNome(condominio.getNome());
        }

        if (condominio.getEndereco() != null && !condominio.getEndereco().isEmpty()) {
            condominioExistente.setEndereco(condominio.getEndereco());
        }

        if (condominio.getCep() != null) {
            condominioExistente.setCep(condominio.getCep());
        }

        if (condominio.getCidade() != null) {
            condominioExistente.setCidade(condominio.getCidade());
        }

        if (condominio.getEstado() != null) {
            condominioExistente.setEstado(condominio.getEstado());
        }

        if (condominio.getTelefone() != null) {
            condominioExistente.setTelefone(condominio.getTelefone());
        }

        if (condominio.getEmail() != null) {
            condominioExistente.setEmail(condominio.getEmail());
        }

        // Removida a referência ao atributo responsavel que não existe mais no modelo

        return condominioRepository.save(condominioExistente);
    }

    @Transactional
    public void deletarCondominio(Long id) {
        Condominio condominio = condominioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condomínio", "id", id));

        if (condominio.getArmarios() != null && !condominio.getArmarios().isEmpty()) {
            throw new RelatedResourceException("condomínio", "armários");
        }

        condominioRepository.deleteById(id);
    }
}
