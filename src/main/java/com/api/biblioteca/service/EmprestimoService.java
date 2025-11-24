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
    @Autowired
    private NotificacaoService notificacaoService;

   // Em service/EmprestimoService.java
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

     @Transactional
    public synchronized Emprestimo criarEmprestimo(Long idUsuario, Long idLivro) {
        // 1. Validar Usuário
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + idUsuario));

        // 2. Validar Livro
        Livro livro = livroRepository.findById(idLivro)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado com o ID: " + idLivro));

        // 3. Verificação de Multas
        if (multaService.verificarSeUsuarioTemMultasPendentes(usuario)) {
            throw new IllegalStateException("Usuário possui multas pendentes e não pode realizar novos empréstimos.");
        }

        // 4. Verificar se usuário já tem empréstimos em andamento
        // (Isso impede que ele tenha mais de um livro emprestado ou reservado aguardando retirada)
        List<StatusEmprestimo> statusesEmAndamento = List.of(
            StatusEmprestimo.ATIVO, 
            StatusEmprestimo.ATRASADO,
            StatusEmprestimo.AGUARDANDO_RETIRADA
        );

        if (emprestimoRepository.existsByUsuarioAndStatusEmprestimoIn(usuario, statusesEmAndamento)) {
            throw new IllegalStateException("Usuário já possui um empréstimo em andamento (ativo, atrasado ou aguardando retirada).");
        }

        // Validação Extra de Segurança: Verifica duplicidade específica para este livro e usuário AGORA
        // Caso a verificação genérica acima falhe por delay de banco de dados
        boolean jaSolicitouEsteLivro = emprestimoRepository.existsByUsuarioAndStatusEmprestimoIn(usuario, List.of(StatusEmprestimo.AGUARDANDO_RETIRADA));
        if (jaSolicitouEsteLivro) {
             throw new IllegalStateException("Solicitação já processada. Atualize a página.");
        }

        // 5. Lógica de Reserva (Sem fila de prioridade estrita)
        Optional<Reserva> reservaDoUsuarioOpt = reservaRepository
                .findByUsuarioAndLivroAndStatusReservaOrderByDataReservaAsc(usuario, livro, StatusReserva.ATIVA)
                .stream().findFirst();

        if (reservaDoUsuarioOpt.isPresent()) {
            // CENÁRIO A: O usuário TEM reserva. 
            Reserva reservaDoUsuario = reservaDoUsuarioOpt.get();
            
            // Consumir a reserva
            reservaDoUsuario.setStatusReserva(StatusReserva.ATENDIDA); 
            reservaRepository.save(reservaDoUsuario);

        } else {
            // CENÁRIO B: O usuário NÃO tem reserva.
            // Verifica se existe ALGUMA reserva ativa para este livro por outra pessoa.
            boolean existeReservaDeOutros = reservaRepository.existsByLivroAndStatusReserva(livro, StatusReserva.ATIVA);
            
            if (existeReservaDeOutros) {
                throw new IllegalStateException("Este livro está reservado por outro usuário.");
            }
        }

        // 6. Verificar Estoque Físico
        if (livro.getQtdDisponivel() <= 0) {
            throw new IllegalStateException("Livro indisponível no estoque.");
        }

        // 7. Atualizar Estoque do Livro
        livro.setQtdDisponivel(livro.getQtdDisponivel() - 1);
        
        if (livro.getQtdDisponivel() == 0) {
            livro.setStatusLivro(StatusLivro.EMPRESTADO); 
        }
        livroRepository.save(livro);

        // 8. Criar o Objeto Empréstimo
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setStatusEmprestimo(StatusEmprestimo.AGUARDANDO_RETIRADA); 
        emprestimo.setDataEmprestimo(LocalDateTime.now()); 
        
        // 9. Salvar Empréstimo
        Emprestimo emprestimoSalvo = emprestimoRepository.save(emprestimo);

        // 10. Envio de E-mail e Notificação
        enviarNotificacoesAssincronas(emprestimoSalvo);

        return emprestimoSalvo;
    }
    

    private void enviarNotificacoesAssincronas(Emprestimo emprestimo) {
        try {
            emailService.enviarEmailAprovacaoEmprestimo(
                emprestimo.getUsuario().getEmail(),
                emprestimo.getUsuario().getNome(),
                emprestimo.getLivro().getTitulo(),
                emprestimo.getId()
            );
        } catch (MessagingException e) {
            System.err.println("Falha ao enviar e-mail de aprovação: " + e.getMessage());
        }

        try {
            notificacaoService.criarNotificacao(
                emprestimo.getUsuario(),
                "Sua solicitação para \"" + emprestimo.getLivro().getTitulo() + "\" foi aprovada! Retire-o em até 48h."
            );
        } catch (Exception e) {
            System.err.println("Erro notificação: " + e.getMessage());
        }
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
            boolean estaAtrasado = LocalDateTime.now().isAfter(emprestimo.getDataPrevistaDevolucao());
            if (estaAtrasado) {
                multaService.criarMulta(emprestimo);
            }
        }
        
        emprestimo.setStatusEmprestimo(StatusEmprestimo.FINALIZADO);

        Livro livro = emprestimo.getLivro();
        livro.setQtdDisponivel(livro.getQtdDisponivel() + 1);

        // Verifica se ainda existem OUTROS empréstimos ativos ou atrasados para este livro
        long outrosEmprestimosAtivos = emprestimoRepository.countByLivroAndStatusEmprestimoIn(
            livro, List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO)
        );

        Optional<Reserva> proximaReservaOpt = reservaRepository.findTopByLivroAndStatusReservaOrderByDataReserva(livro, StatusReserva.ATIVA);
        
        if (proximaReservaOpt.isPresent()) {
            // Se há reservas pendentes, a cópia devolvida fica reservada para o próximo da fila.
            livro.setStatusLivro(StatusLivro.RESERVADO);

            //Notificação
            try{
                Reserva proximaReserva = proximaReservaOpt.get();
                notificacaoService.criarNotificacao(
                    proximaReserva.getUsuario(),
                    "Boas notícias! O livro \"" + livro.getTitulo() + "\" que você reservou está disponível para retiada.");
            }catch (Exception e){
                System.err.println("Erro ao notificar próximo da reserva: " + e.getMessage());
            }
            
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

        //Notificação
        try{
            notificacaoService.criarNotificacao(
                emprestimo.getUsuario(),
                "Obrigado por devolver o livro \"" + livro.getTitulo() + "\".");
        }catch (Exception e){
            System.err.println("Erro ao criar notificação (devolução): " + e.getMessage());
        }
        return emprestimoRepository.save(emprestimo);
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