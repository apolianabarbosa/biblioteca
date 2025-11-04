package com.api.biblioteca.service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

import jakarta.mail.MessagingException;
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
    @Autowired
    private EmailService emailService;

   // Em service/EmprestimoService.java
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @Transactional
    public Emprestimo criarEmprestimo(Long idUsuario, Long idLivro) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + idUsuario));

        Livro livro = livroRepository.findById(idLivro)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado com o ID: " + idLivro));

        if (multaService.verificarSeUsuarioTemMultasPendentes(usuario)) {
            throw new IllegalStateException("Usuário possui multas pendentes e não pode realizar novos empréstimos.");
        }
        
        // Define quais status são considerados "em andamento"
        List<StatusEmprestimo> statusesEmAndamento = List.of(
            StatusEmprestimo.ATIVO, 
            StatusEmprestimo.ATRASADO,
            StatusEmprestimo.AGUARDANDO_RETIRADA
        );

        // Verifica no banco se o usuário já tem empréstimos nesses status
        if (emprestimoRepository.existsByUsuarioAndStatusEmprestimoIn(usuario, statusesEmAndamento)) {
            throw new IllegalStateException("Usuário já possui um empréstimo em andamento (ativo, atrasado ou aguardando retirada).");
        }
        
        List<Reserva> reservasAtivas = reservaRepository.findByUsuarioAndLivroAndStatusReservaOrderByDataReservaAsc(usuario, livro, StatusReserva.ATIVA);

        if (!reservasAtivas.isEmpty()) {
            // CASO 1: O empréstimo está atendendo a uma reserva.
            // A quantidade já foi decrementada na criação da reserva, então SÓ atualizamos o status.
            Reserva reservaParaAtender = reservasAtivas.get(0);
            reservaParaAtender.setStatusReserva(StatusReserva.ATENDIDA);
            reservaRepository.save(reservaParaAtender);
        } else {
            // CASO 2: É um empréstimo direto, sem reserva prévia.
            // Neste caso, precisamos decrementar a quantidade disponível.
            livro.setQtdDisponivel(livro.getQtdDisponivel() - 1);
        }
        
        // O status do livro é atualizado em ambos os casos
        if (livro.getQtdDisponivel() == 0) {
            livro.setStatusLivro(StatusLivro.EMPRESTADO);
        }

        livroRepository.save(livro);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setStatusEmprestimo(StatusEmprestimo.AGUARDANDO_RETIRADA);
        

        Emprestimo emprestimoSalvo = emprestimoRepository.save(emprestimo);

        try{
            emailService.enviarEmailAprovacaoEmprestimo(
            emprestimoSalvo.getUsuario().getEmail(),
            emprestimoSalvo.getUsuario().getNome(),
            emprestimoSalvo.getLivro().getTitulo(),
            emprestimoSalvo.getId()
            );
        } catch (MessagingException e){
            System.err.println("Falha ao enviar e-mail de confirmação (sem data): " + e.getMessage());
        }
        
        return emprestimoSalvo;
    }

    @Transactional
    public Emprestimo confirmarRetirada(Long idEmprestimo){
        Emprestimo emprestimo = emprestimoRepository.findById(idEmprestimo)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado com o ID: " + idEmprestimo));

        if (emprestimo.getStatusEmprestimo() != StatusEmprestimo.AGUARDANDO_RETIRADA) {
            throw new IllegalStateException("Este empréstimo não está aguardando retirada. Status atual: " + emprestimo.getStatusEmprestimo());
        }

        emprestimo.setStatusEmprestimo((StatusEmprestimo.ATIVO));
        emprestimo.setDataEmprestimo(LocalDateTime.now());

        // Teste em minutos
        emprestimo.setDataPrevistaDevolucao(LocalDateTime.now().plusMinutes(1));

        //Produção em 15 dias
        // emprestimo.setDataPrevistaDevolucao(LocalDateTime.now().plusDays(15));

        Emprestimo emprestimoSalvo = emprestimoRepository.save(emprestimo);

        try{
            emailService.enviarEmailRetiradaConfirmada(
                emprestimoSalvo.getUsuario().getEmail(),
                emprestimoSalvo.getUsuario().getNome(),
                emprestimoSalvo.getLivro().getTitulo(),
                emprestimoSalvo.getId(),
                emprestimoSalvo.getDataPrevistaDevolucao(),
                DATE_FORMATTER
            );
        } catch (MessagingException e) {
            System.err.println("Falha ao enviar e-mail de confirmação de retirada: " + e.getMessage());
        }

        return emprestimoSalvo;
    }


    @Transactional
    public Emprestimo devolverLivro(Long idEmprestimo) {
        Emprestimo emprestimo = emprestimoRepository.findById(idEmprestimo)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado com o ID: " + idEmprestimo));

        // A verificação de "FINALIZADO" deve acontecer antes de qualquer outra lógica de negócio.
        if (emprestimo.getStatusEmprestimo() == StatusEmprestimo.FINALIZADO) {
            throw new IllegalStateException("Este empréstimo já foi finalizado.");
        }

        if (emprestimo.getStatusEmprestimo() == StatusEmprestimo.ATIVO || emprestimo.getStatusEmprestimo() == StatusEmprestimo.ATRASADO) {
            boolean estaAtrasado = LocalDateTime.now().isAfter(emprestimo.getDataPrevistaDevolucao());
            if (estaAtrasado) {
                multaService.criarMulta(emprestimo);
            }
        }
        
        // Primeiro, finalizamos o empréstimo atual
        emprestimo.setStatusEmprestimo(StatusEmprestimo.FINALIZADO);

        // --- LÓGICA DE ATUALIZAÇÃO DO LIVRO CORRIGIDA E MAIS ROBUSTA ---
        Livro livro = emprestimo.getLivro();
        livro.setQtdDisponivel(livro.getQtdDisponivel() + 1); // Incrementa a quantidade de volta

        // Verifica se ainda existem OUTROS empréstimos ativos ou atrasados para este livro
        long outrosEmprestimosAtivos = emprestimoRepository.countByLivroAndStatusEmprestimoIn(
            livro, List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO)
        );

        boolean haReservas = reservaRepository.existsByLivroAndStatusReserva(livro, StatusReserva.ATIVA);

        if (haReservas) {
            // Se há reservas pendentes, a cópia devolvida fica reservada para o próximo da fila.
            livro.setStatusLivro(StatusLivro.RESERVADO);
        } else if (outrosEmprestimosAtivos == 0) {
            // Se NÃO há reservas E NENHUMA outra cópia está emprestada,
            // o livro está verdadeiramente disponível.
            livro.setStatusLivro(StatusLivro.DISPONIVEL);
        } else {
            // Se não há reservas, mas outras cópias ainda estão emprestadas,
            // o status deve ser DISPONIVEL, pois agora temos pelo menos uma cópia.
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
            emprestimo.getLivro().getTitulo(),
            emprestimo.getLivro().getAutor()
        ),
        new UsuarioResumidoDTO(
            emprestimo.getUsuario().getId(),
            emprestimo.getUsuario().getNome()
        )
    );
    }

}