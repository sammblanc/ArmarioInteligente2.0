package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Compartimento;
import br.com.unit.tokseg.armariointeligente.model.Reserva;
import br.com.unit.tokseg.armariointeligente.model.StatusReserva;
import br.com.unit.tokseg.armariointeligente.model.Usuario;
import br.com.unit.tokseg.armariointeligente.repository.CompartimentoRepository;
import br.com.unit.tokseg.armariointeligente.repository.ReservaRepository;
import br.com.unit.tokseg.armariointeligente.repository.UsuarioRepository;
import br.com.unit.tokseg.armariointeligente.util.ValidationUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private CompartimentoRepository compartimentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompartimentoService compartimentoService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Reserva criarReserva(Reserva reserva) {
        if (reserva == null) {
            throw new BadRequestException("Reserva não pode ser nula");
        }
        if (reserva.getDataInicio() == null) {
            throw new BadRequestException("Data de início não pode ser nula");
        }
        if (reserva.getDataFim() == null) {
            throw new BadRequestException("Data de fim não pode ser nula");
        }
        if (reserva.getCompartimento() == null || reserva.getCompartimento().getId() == null) {
            throw new BadRequestException("Compartimento é obrigatório");
        }
        if (reserva.getUsuario() == null || reserva.getUsuario().getId() == null) {
            throw new BadRequestException("Usuário é obrigatório");
        }

        // Verificar se as datas são válidas
        if (reserva.getDataInicio().isAfter(reserva.getDataFim())) {
            throw new BadRequestException("Data de início não pode ser posterior à data de fim");
        }
        if (reserva.getDataInicio().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Data de início não pode ser no passado");
        }

        // Validações adicionais
        ValidationUtils.validateFutureDate(reserva.getDataInicio(), "Data de início");
        ValidationUtils.validateFutureDate(reserva.getDataFim(), "Data de fim");
        ValidationUtils.validateDateRange(reserva.getDataInicio(), reserva.getDataFim());
        if (reserva.getObservacao() != null) {
            ValidationUtils.validateStringLength(reserva.getObservacao(), "Observação", 0, 1000);
        }

        Compartimento compartimento = compartimentoRepository.findById(reserva.getCompartimento().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Compartimento", "id", reserva.getCompartimento().getId()));

        Usuario usuario = usuarioRepository.findById(reserva.getUsuario().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", reserva.getUsuario().getId()));

        // Verificar se o compartimento está disponível para o período
        if (compartimento.getOcupado()) {
            throw new BadRequestException("O compartimento selecionado está ocupado");
        }

        // Definir valores padrão
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setCompartimento(compartimento);
        reserva.setUsuario(usuario);

        // Atualizar status do compartimento
        compartimentoService.atualizarStatusCompartimento(compartimento.getId(), true);

        Reserva novaReservaSalva = reservaRepository.save(reserva);
        logger.info("Reserva criada com sucesso - ID: {}, Usuário: {}, Compartimento: {}",
                novaReservaSalva.getId(), novaReservaSalva.getUsuario().getEmail(),
                novaReservaSalva.getCompartimento().getNumero());
        notificationService.enviarNotificacaoReservaConfirmada(novaReservaSalva);
        return novaReservaSalva;
    }

    @Transactional
    public Reserva cancelarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        if (reserva.getStatus() != StatusReserva.CONFIRMADA && reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new BadRequestException("Esta reserva não pode ser cancelada");
        }

        reserva.setStatus(StatusReserva.CANCELADA);

        // Liberar o compartimento
        compartimentoService.atualizarStatusCompartimento(reserva.getCompartimento().getId(), false);

        Reserva reservaCancelada = reservaRepository.save(reserva);
        notificationService.enviarNotificacaoReservaCancelada(reservaCancelada);
        return reservaCancelada;
    }

    @Transactional
    public Reserva concluirReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        if (reserva.getStatus() != StatusReserva.CONFIRMADA) {
            throw new BadRequestException("Esta reserva não pode ser concluída");
        }

        reserva.setStatus(StatusReserva.CONCLUIDA);

        // Liberar o compartimento
        compartimentoService.atualizarStatusCompartimento(reserva.getCompartimento().getId(), false);

        return reservaRepository.save(reserva);
    }

    @Transactional
    public List<Reserva> listarReservas() {
        return reservaRepository.findAll();
    }

    @Transactional
    public List<Reserva> listarReservasPorCompartimento(Long compartimentoId) {
        if (!compartimentoRepository.existsById(compartimentoId)) {
            throw new ResourceNotFoundException("Compartimento", "id", compartimentoId);
        }
        return reservaRepository.findByCompartimentoId(compartimentoId);
    }

    @Transactional
    public List<Reserva> listarReservasPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário", "id", usuarioId);
        }
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public List<Reserva> listarReservasPorStatus(StatusReserva status) {
        return reservaRepository.findByStatus(status);
    }

    @Transactional
    public Optional<Reserva> buscarReservaPorId(Long id) {
        return reservaRepository.findById(id);
    }

    @Transactional
    public List<Reserva> listarReservasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return reservaRepository.findByDataInicioBetween(inicio, fim);
    }
}
