package com.api.biblioteca.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.biblioteca.dtos.MultaDTO;
import com.api.biblioteca.model.Emprestimo;
import com.api.biblioteca.model.Multa;
import com.api.biblioteca.model.Multa.StatusMulta;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.repository.MultaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MultaService {

    @Autowired
    private MultaRepository multaRepository;

    // private static final BigDecimal VALOR_DIARIO_MULTA = new BigDecimal("1.50");
    private static final BigDecimal VALOR_POR_MINUTO_MULTA = new BigDecimal("0.50");

    @Transactional
    public Multa criarMulta(Emprestimo emprestimo) {
        // long diasAtraso = ChronoUnit.DAYS.between(emprestimo.getDataPrevistaDevolucao(), LocalDateTime.now());
        long minutosAtraso = ChronoUnit.MINUTES.between(emprestimo.getDataPrevistaDevolucao(), LocalDateTime.now());
        // Garante que a multa seja aplicada apenas para atrasos de pelo menos um dia.
        // if (diasAtraso <= 0) {
        //     return null; 
        // }
        if (minutosAtraso <= 0) {
            return null; 
        }

        // BigDecimal valorTotal = VALOR_DIARIO_MULTA.multiply(new BigDecimal(diasAtraso));
        BigDecimal valorTotal = VALOR_POR_MINUTO_MULTA.multiply(new BigDecimal(minutosAtraso));
        

        Multa multa = new Multa();
        multa.setEmprestimo(emprestimo);
        multa.setValor(valorTotal);
        multa.setStatusMulta(StatusMulta.PENDENTE);
        multa.setDataMulta(LocalDateTime.now());

        return multaRepository.save(multa);
    }

    @Transactional
    public Multa pagarMulta(Long idMulta) {
        Multa multa = multaRepository.findById(idMulta)
                .orElseThrow(() -> new EntityNotFoundException("Multa não encontrada com o ID: " + idMulta));

        if (multa.getStatusMulta() == StatusMulta.PAGO) {
            throw new IllegalStateException("A multa já foi paga.");
        }

        multa.setStatusMulta(StatusMulta.PAGO);
        return multaRepository.save(multa);
    }

    public boolean verificarSeUsuarioTemMultasPendentes(Usuario usuario) {
        List<Multa> multasPendentes = multaRepository.findByEmprestimoUsuarioAndStatusMulta(usuario, StatusMulta.PENDENTE);
        return !multasPendentes.isEmpty();
    }

    public List<Multa> listarMultasPorUsuario(Usuario usuario) {
        return multaRepository.findByEmprestimoUsuario(usuario);
    }

    public List<Multa> listarMultasPorStatus(StatusMulta statusMulta) {
        return multaRepository.findByStatusMulta(statusMulta);
    }

    public Optional<Multa> buscarPorId(Long id) {
        return multaRepository.findById(id);
    }

    public List<Multa> listarTodas() {
        return multaRepository.findAll();
    }

    public MultaDTO toDTO(Multa multa) {
    return new MultaDTO(
        multa.getId(),
        multa.getValor(),
        multa.getStatusMulta(),
        multa.getEmprestimo().getId(),
        multa.getEmprestimo().getDataEmprestimo(),
        multa.getEmprestimo().getLivro().getTitulo(),
        multa.getEmprestimo().getUsuario().getNome()
    );
    }
    
}