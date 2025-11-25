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

    // Retorna os números gerais para os "Cards" do Dashboard
    public DashboardStatsDTO obterEstatisticasGerais() {
        long totalLivros = livroRepository.count();
        long totalUsuarios = usuarioRepository.count();
        // Assumindo que você tem um método countByStatusEmprestimoNot(FINALIZADO)
        long emprestimosAtivos = emprestimoRepository.countByStatusEmprestimoNot(Emprestimo.StatusEmprestimo.FINALIZADO);
        long reservasAtivas = reservaRepository.countByStatusReserva(Reserva.StatusReserva.ATIVA);

        return new DashboardStatsDTO(totalLivros, totalUsuarios, emprestimosAtivos, reservasAtivas);
    }

    // Retorna o Top 5 livros mais emprestados
    public List<RelatorioItemDTO> obterTopLivrosEmprestados() {
        Pageable limit = PageRequest.of(0, 5); // Página 0, tamanho 5
        return emprestimoRepository.findLivrosMaisEmprestados(limit);
    }

    // Retorna o Top 5 livros mais reservados
    public List<RelatorioItemDTO> obterTopLivrosReservados() {
        Pageable limit = PageRequest.of(0, 5);
        return reservaRepository.findLivrosMaisReservados(limit);
    }
}