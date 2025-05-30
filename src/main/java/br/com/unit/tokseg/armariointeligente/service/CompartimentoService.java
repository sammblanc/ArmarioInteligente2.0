package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.RelatedResourceException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceAlreadyExistsException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Armario;
import br.com.unit.tokseg.armariointeligente.model.Compartimento;
import br.com.unit.tokseg.armariointeligente.repository.ArmarioRepository;
import br.com.unit.tokseg.armariointeligente.repository.CompartimentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class CompartimentoService {

    @Autowired
    private CompartimentoRepository compartimentoRepository;

    @Autowired
    private ArmarioRepository armarioRepository;

    @Transactional
    public Compartimento criarCompartimento(Compartimento compartimento) {
        if (compartimento == null) {
            throw new BadRequestException("Compartimento não pode ser nulo");
        }
        if (compartimento.getNumero() == null || compartimento.getNumero().isEmpty()) {
            throw new BadRequestException("Número do compartimento não pode ser nulo ou vazio");
        }
        if (compartimento.getArmario() == null || compartimento.getArmario().getId() == null) {
            throw new BadRequestException("Armário é obrigatório");
        }

        Armario armario = armarioRepository.findById(compartimento.getArmario().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Armário", "id", compartimento.getArmario().getId()));

        compartimentoRepository.findByNumeroAndArmarioId(compartimento.getNumero(), armario.getId())
                .ifPresent(c -> {
                    throw new ResourceAlreadyExistsException("Compartimento", "número", compartimento.getNumero());
                });

        // Gerar código de acesso aleatório se não for fornecido
        if (compartimento.getCodigoAcesso() == null || compartimento.getCodigoAcesso().isEmpty()) {
            compartimento.setCodigoAcesso(gerarCodigoAcesso());
        }

        compartimento.setArmario(armario);
        return compartimentoRepository.save(compartimento);
    }

    @Transactional
    public List<Compartimento> listarCompartimentos() {
        return compartimentoRepository.findAll();
    }

    @Transactional
    public List<Compartimento> listarCompartimentosPorArmario(Long armarioId) {
        if (!armarioRepository.existsById(armarioId)) {
            throw new ResourceNotFoundException("Armário", "id", armarioId);
        }
        return compartimentoRepository.findByArmarioId(armarioId);
    }

    @Transactional
    public List<Compartimento> listarCompartimentosPorStatus(Boolean ocupado) {
        return compartimentoRepository.findByOcupado(ocupado);
    }

    @Transactional
    public Optional<Compartimento> buscarCompartimentoPorId(Long id) {
        return compartimentoRepository.findById(id);
    }

    @Transactional
    public Compartimento atualizarCompartimento(Long id, Compartimento compartimento) {
        Compartimento compartimentoExistente = compartimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartimento", "id", id));

        // Verifica se o novo número já existe para outro compartimento no mesmo armário
        if (compartimento.getNumero() != null && !compartimento.getNumero().isEmpty()) {
            Long armarioId = compartimento.getArmario() != null && compartimento.getArmario().getId() != null ?
                    compartimento.getArmario().getId() : compartimentoExistente.getArmario().getId();

            Optional<Compartimento> compartimentoComMesmoNumero = compartimentoRepository
                    .findByNumeroAndArmarioId(compartimento.getNumero(), armarioId);

            if (compartimentoComMesmoNumero.isPresent() && !compartimentoComMesmoNumero.get().getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Compartimento", "número", compartimento.getNumero());
            }
            compartimentoExistente.setNumero(compartimento.getNumero());
        }

        // Atualiza o armário se fornecido
        if (compartimento.getArmario() != null && compartimento.getArmario().getId() != null) {
            Armario armario = armarioRepository.findById(compartimento.getArmario().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Armário", "id", compartimento.getArmario().getId()));
            compartimentoExistente.setArmario(armario);
        }

        if (compartimento.getTamanho() != null) {
            compartimentoExistente.setTamanho(compartimento.getTamanho());
        }

        if (compartimento.getOcupado() != null) {
            compartimentoExistente.setOcupado(compartimento.getOcupado());
        }

        if (compartimento.getCodigoAcesso() != null && !compartimento.getCodigoAcesso().isEmpty()) {
            compartimentoExistente.setCodigoAcesso(compartimento.getCodigoAcesso());
        }

        return compartimentoRepository.save(compartimentoExistente);
    }

    @Transactional
    public void deletarCompartimento(Long id) {
        Compartimento compartimento = compartimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartimento", "id", id));

        if (compartimento.getEntregas() != null && !compartimento.getEntregas().isEmpty()) {
            throw new RelatedResourceException("compartimento", "entregas");
        }

        compartimentoRepository.deleteById(id);
    }

    @Transactional
    public Compartimento atualizarStatusCompartimento(Long id, Boolean ocupado) {
        Compartimento compartimento = compartimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartimento", "id", id));

        compartimento.setOcupado(ocupado);
        return compartimentoRepository.save(compartimento);
    }

    @Transactional
    public Compartimento gerarNovoCodigoAcesso(Long id) {
        Compartimento compartimento = compartimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartimento", "id", id));

        compartimento.setCodigoAcesso(gerarCodigoAcesso());
        return compartimentoRepository.save(compartimento);
    }

    private String gerarCodigoAcesso() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000); // Gera um número de 6 dígitos
        return String.valueOf(codigo);
    }
}
