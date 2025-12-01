package com.api.biblioteca.service;

import com.api.biblioteca.dtos.DashboardStatsDTO;
import com.api.biblioteca.dtos.RelatorioItemDTO;
import com.api.biblioteca.model.Emprestimo;
import com.api.biblioteca.model.Reserva;
import com.api.biblioteca.repository.EmprestimoRepository;
import com.api.biblioteca.repository.LivroRepository;
import com.api.biblioteca.repository.ReservaRepository;
import com.api.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioService {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    public DashboardStatsDTO obterEstatisticasGerais(LocalDate startDate, LocalDate endDate) {
        
        if (startDate == null && endDate == null) {
            long totalLivros = livroRepository.count();
            long totalUsuarios = usuarioRepository.count();
            long emprestimosAtivos = emprestimoRepository.countByStatusEmprestimoNot(Emprestimo.StatusEmprestimo.FINALIZADO);
            long reservasAtivas = reservaRepository.countByStatusReserva(Reserva.StatusReserva.ATIVA);

            return new DashboardStatsDTO(totalLivros, totalUsuarios, emprestimosAtivos, reservasAtivas);
        }
        
        
        return new DashboardStatsDTO(
            livroRepository.count(),
            usuarioRepository.count(),
            0L,
            0L
        );
    }

    public List<RelatorioItemDTO> obterTopLivrosEmprestados(LocalDate startDate, LocalDate endDate) {
        Pageable limit = PageRequest.of(0, 5);
        return emprestimoRepository.findLivrosMaisEmprestadosComFiltro(startDate, endDate, limit);
    }

    public List<RelatorioItemDTO> obterTopLivrosReservados(LocalDate startDate, LocalDate endDate) {
        Pageable limit = PageRequest.of(0, 5);
        return reservaRepository.findLivrosMaisReservadosComFiltro(startDate, endDate, limit);
    }
}