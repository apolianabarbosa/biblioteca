package com.api.biblioteca.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.biblioteca.dtos.EmprestimoDTO;
import com.api.biblioteca.dtos.LivroResumidoDTO;
import com.api.biblioteca.dtos.UsuarioResumidoDTO;
import com.api.biblioteca.model.Emprestimo;
import com.api.biblioteca.model.Emprestimo.StatusEmprestimo;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.Livro.StatusLivro;
import com.api.biblioteca.model.Reserva;
import com.api.biblioteca.model.Reserva.StatusReserva;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.repository.EmprestimoRepository;
import com.api.biblioteca.repository.LivroRepository;
import com.api.biblioteca.repository.ReservaRepository;
import com.api.biblioteca.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EmprestimoService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;
    @Autowired
    private LivroRepository livroRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private MultaService multaService;

    @Transactional
    public Emprestimo criarEmprestimo(Long idUsuario, Long idLivro) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + idUsuario));

        Livro livro = livroRepository.findById(idLivro)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado com o ID: " + idLivro));

        if (multaService.verificarSeUsuarioTemMultasPendentes(usuario)) {
            throw new IllegalStateException("Usuário possui multas pendentes e não pode realizar novos empréstimos.");
        }

        List<Reserva> reservasAtivas = reservaRepository.findByUsuarioAndLivroAndStatusReservaOrderByDataReservaAsc(usuario, livro, StatusReserva.ATIVA);

        if (!reservasAtivas.isEmpty()) {
            Reserva reservaParaAtender = reservasAtivas.get(0); // Pega a mais antiga da fila
            reservaParaAtender.setStatusReserva(StatusReserva.ATENDIDA);
            reservaRepository.save(reservaParaAtender);
        }

        livro.setQtdDisponivel(livro.getQtdDisponivel() - 1);
        if (livro.getQtdDisponivel() == 0) {
            livro.setStatusLivro(StatusLivro.EMPRESTADO);
        }
        livroRepository.save(livro);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setStatusEmprestimo(StatusEmprestimo.ATIVO);

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo devolverLivro(Long idEmprestimo) {
        Emprestimo emprestimo = emprestimoRepository.findById(idEmprestimo)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado com o ID: " + idEmprestimo));

        if (emprestimo.getStatusEmprestimo() == StatusEmprestimo.FINALIZADO) {
            throw new IllegalStateException("Este empréstimo já foi finalizado.");
        }

        // Verifica se a devolução está atrasada ANTES de alterar o status
        boolean estaAtrasado = LocalDateTime.now().isAfter(emprestimo.getDataPrevistaDevolucao());
        if (estaAtrasado) {
            multaService.criarMulta(emprestimo); // Cria a multa se houver atraso
        }
        
        // Define o empréstimo como FINALIZADO, pois o livro foi devolvido
        emprestimo.setStatusEmprestimo(StatusEmprestimo.FINALIZADO);

        Livro livro = emprestimo.getLivro();
        livro.setQtdDisponivel(livro.getQtdDisponivel() + 1);

        // Verifica se há reservas ativas para este livro
        List<Reserva> reservasAtivas = reservaRepository.findFirstByLivroAndStatusReservaOrderByDataReservaAsc(livro, StatusReserva.ATIVA);

        if (!reservasAtivas.isEmpty()) {
            livro.setStatusLivro(StatusLivro.RESERVADO);
        } else {
            livro.setStatusLivro(StatusLivro.DISPONIVEL);
        }
        livroRepository.save(livro);

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public void verificarEAtualizarAtrasos() {
        List<Emprestimo> emprestimosAtivos = emprestimoRepository.findByStatusEmprestimo(StatusEmprestimo.ATIVO);
        for (Emprestimo emprestimo : emprestimosAtivos) {
            if (LocalDateTime.now().isAfter(emprestimo.getDataPrevistaDevolucao())) {
                emprestimo.setStatusEmprestimo(StatusEmprestimo.ATRASADO);
                emprestimoRepository.save(emprestimo);
            }
        }
    }

    public List<Emprestimo> listarTodos() {
        return emprestimoRepository.findAllByOrderByDataEmprestimoDesc();
    }

    public List<Emprestimo> listarPorUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + idUsuario));
        return emprestimoRepository.findByUsuario(usuario);
    }

    public Optional<Emprestimo> buscarPorId(Long id) {
        return emprestimoRepository.findById(id);
    }

    public EmprestimoDTO toDTO(Emprestimo emprestimo) {
    return new EmprestimoDTO(
        emprestimo.getId(),
        emprestimo.getDataEmprestimo(),
        emprestimo.getDataPrevistaDevolucao(),
        emprestimo.getStatusEmprestimo(),
        new LivroResumidoDTO(
            emprestimo.getLivro().getId(),
            emprestimo.getLivro().getTitulo()
        ),
        new UsuarioResumidoDTO(
            emprestimo.getUsuario().getId(),
            emprestimo.getUsuario().getNome()
        )
    );
    }

}