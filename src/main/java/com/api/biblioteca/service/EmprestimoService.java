package com.api.biblioteca.service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.api.biblioteca.model.RespostaModel;
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
    @Autowired
    private NotificacaoService notificacaoService;

   // Em service/EmprestimoService.java
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @Transactional
    public Emprestimo criarEmprestimo(Long idUsuario, Long idLivro) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + idUsuario));

        Livro livro = livroRepository.findById(idLivro)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado com o ID: " + idLivro));

        // Verifica multas
        if (multaService.verificarSeUsuarioTemMultasPendentes(usuario)) {
            throw new IllegalStateException("Usuário possui multas pendentes e não pode realizar novos empréstimos.");
        }

        // Impede múltiplos empréstimos em andamento
        List<StatusEmprestimo> statusesImpedem = List.of(StatusEmprestimo.AGUARDANDO_RETIRADA, StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO);
        if (emprestimoRepository.existsByUsuarioAndStatusEmprestimoIn(usuario, statusesImpedem)) {
            throw new IllegalStateException("Usuário já possui um empréstimo em andamento.");
        }

        // Reserva do próprio usuário (se houver) - só pode atender se for o primeiro da fila
        Optional<Reserva> reservaDoUsuarioOpt = reservaRepository
                .findByUsuarioAndLivroAndStatusReservaOrderByDataReservaAsc(usuario, livro, StatusReserva.ATIVA)
                .stream().findFirst();

        if (reservaDoUsuarioOpt.isPresent()) {
            Reserva reservaDoUsuario = reservaDoUsuarioOpt.get();
            boolean existeAlguemNaFrente = reservaRepository.existsByLivroAndStatusReservaAndDataReservaBefore(
                livro, StatusReserva.ATIVA, reservaDoUsuario.getDataReserva()
            );

            // atende a reserva do usuário (não decrementa aqui se você já decrementou quando criou a reserva;
            // se você NÃO decrementa na criação da reserva, então decrementar agora)
            reservaDoUsuario.setStatusReserva(StatusReserva.ATENDIDA);
            reservaRepository.save(reservaDoUsuario);
        } else {
            // se não tem reserva do usuário, não pode haver fila de outros
            boolean existemReservasParaOLivro = reservaRepository.existsByLivroAndStatusReserva(livro, StatusReserva.ATIVA);
            if (existemReservasParaOLivro) {
                throw new IllegalStateException("Este livro possui lista de espera. Faça uma reserva para entrar na fila.");
            }

            if (livro.getQtdDisponivel() <= 0) {
                throw new IllegalStateException("Livro indisponível no estoque.");
            }
            // decrementa apenas aqui (caso reserva não tenha decrementado antes)
            livro.setQtdDisponivel(livro.getQtdDisponivel() - 1);
        }

        if (livro.getQtdDisponivel() <= 0) {
            livro.setStatusLivro(StatusLivro.INDISPONIVEL);
        }
        livroRepository.save(livro);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setStatusEmprestimo(StatusEmprestimo.AGUARDANDO_RETIRADA);
        emprestimo.setDataEmprestimo(LocalDateTime.now());

        Emprestimo salvo = emprestimoRepository.save(emprestimo);

        // envio de email — ajuste para a assinatura correta do método
        try {
            emailService.enviarEmailAprovacaoEmprestimo(
                salvo.getUsuario().getEmail(),
                salvo.getUsuario().getNome(),
                salvo.getLivro().getTitulo(),
                salvo.getId()
            );
        } catch (MessagingException e) {
            System.err.println("Falha ao enviar e-mail de aprovação: " + e.getMessage());
        }

        try {
            notificacaoService.criarNotificacao(salvo.getUsuario(),
                "Sua solicitação para \"" + salvo.getLivro().getTitulo() + "\" foi aprovada! Retire-o em até 48h.");
        } catch (Exception e) {
            System.err.println("Erro notificação: " + e.getMessage());
        }

    return salvo;
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

        
        //Notificação
        try{
            String dataFormatada = emprestimoSalvo.getDataPrevistaDevolucao().format(DATE_FORMATTER);
            notificacaoService.criarNotificacao(
                emprestimoSalvo.getUsuario(),
                "Você retirou o livro \"" + emprestimoSalvo.getLivro().getTitulo() + "\". Devolução em: " + dataFormatada);
        }catch (Exception e){
            System.err.println("Erro ao criar notificação (confirmar retirada): " + e.getMessage());
        }
        return emprestimoSalvo;
    }


   @Transactional
    public Emprestimo devolverLivro(Long idEmprestimo) {
        Emprestimo emprestimo = emprestimoRepository.findById(idEmprestimo)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado com o ID: " + idEmprestimo));

        if (emprestimo.getStatusEmprestimo() == StatusEmprestimo.FINALIZADO) {
            throw new IllegalStateException("Este empréstimo já foi finalizado.");
        }

        if (emprestimo.getStatusEmprestimo() == StatusEmprestimo.ATIVO || emprestimo.getStatusEmprestimo() == StatusEmprestimo.ATRASADO) {
            boolean estaAtrasado = emprestimo.getDataPrevistaDevolucao() != null && LocalDateTime.now().isAfter(emprestimo.getDataPrevistaDevolucao());
            if (estaAtrasado) {
                multaService.criarMulta(emprestimo);
            }
        }

        // Finaliza empréstimo
        emprestimo.setStatusEmprestimo(StatusEmprestimo.FINALIZADO);

        emprestimo.setDataDevolucaoReal(LocalDateTime.now());

        // Re-fetch do livro do banco para evitar estado stale
        Livro livro = livroRepository.findById(emprestimo.getLivro().getId())
                .orElseThrow(() -> new EntityNotFoundException("Livro associado não encontrado"));

        // Incrementa apenas uma vez e não ultrapassa o total
        int novoDisponivel = Math.min(livro.getQtdDisponivel() + 1, livro.getQtdTotal());
        livro.setQtdDisponivel(novoDisponivel);

        // Verifica se há reservas pendentes
        Optional<Reserva> proximaReservaOpt = reservaRepository.findTopByLivroAndStatusReservaOrderByDataReserva(livro, StatusReserva.ATIVA);

        if (proximaReservaOpt.isPresent()) {
            livro.setStatusLivro(StatusLivro.INDISPONIVEL);

            // notifica o próximo
            try {
                Reserva proxima = proximaReservaOpt.get();
                notificacaoService.criarNotificacao(proxima.getUsuario(),
                    "Boas notícias! O livro \"" + livro.getTitulo() + "\" que você reservou está disponível para retirada.");
            } catch (Exception e) {
                System.err.println("Erro ao notificar próximo da reserva: " + e.getMessage());
            }
        } else {
            // se não há reservas e pelo menos 1 disponível, marcar disponível
            if (livro.getQtdDisponivel() > 0 && emprestimoRepository.countByLivroAndStatusEmprestimoIn(livro, List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO)) == 0) {
                livro.setStatusLivro(StatusLivro.DISPONIVEL);
            }else{
                livro.setStatusLivro(StatusLivro.INDISPONIVEL);
        }

        }

        livroRepository.save(livro);

        // Notificação ao usuário que devolveu
        try {
            notificacaoService.criarNotificacao(emprestimo.getUsuario(),
                "Obrigado por devolver o livro \"" + livro.getTitulo() + "\".");
        } catch (Exception e) {
            System.err.println("Erro ao criar notificação (devolução): " + e.getMessage());
        }

        return emprestimoRepository.save(emprestimo);
}
    @Transactional
    public ResponseEntity<RespostaModel> deletarLivro(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado com o ID: " + id));

        // Marca como inativo (soft-delete) em vez de apagar
        livro.setStatusLivro(StatusLivro.INDISPONIVEL);
        livroRepository.save(livro);

        return ResponseEntity.ok(new RespostaModel("Livro deletado com sucesso."));
    }

    @Transactional
    public void verificarEAtualizarAtrasos() {
        List<Emprestimo> emprestimosAtivos = emprestimoRepository.findByStatusEmprestimo(StatusEmprestimo.ATIVO);
        for (Emprestimo emprestimo : emprestimosAtivos) {
            if (LocalDateTime.now().isAfter(emprestimo.getDataPrevistaDevolucao())) {
                emprestimo.setStatusEmprestimo(StatusEmprestimo.ATRASADO);
                emprestimoRepository.save(emprestimo);

                //Notificação
                try{
                    notificacaoService.criarNotificacao(
                        emprestimo.getUsuario(),
                        "Atenção! Seu empréstimo do livro \"" + emprestimo.getLivro().getTitulo() + "\" está atrasado.");
                }catch (Exception e){
                    System.err.println("Erro ao criar notificação (empréstimo  atrasado): " + e.getMessage());
                }
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
        emprestimo.getDataDevolucaoReal(),
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