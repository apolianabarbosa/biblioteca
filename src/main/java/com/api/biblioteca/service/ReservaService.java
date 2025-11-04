package com.api.biblioteca.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.api.biblioteca.dtos.ReservaDTO;
import com.api.biblioteca.dtos.UsuarioResumidoDTO;
import com.api.biblioteca.dtos.LivroResumidoDTO;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.Reserva;
import com.api.biblioteca.model.Reserva.StatusReserva;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.model.Multa.StatusMulta;
import com.api.biblioteca.repository.LivroRepository;
import com.api.biblioteca.repository.MultaRepository;
import com.api.biblioteca.repository.ReservaRepository;
import com.api.biblioteca.repository.UsuarioRepository;

import jakarta.transaction.Transactional;


@Service
public class ReservaService {

    private final ReservaRepository ry;
    private final UsuarioRepository ur;
    private final LivroRepository lr;
    private final MultaRepository mr;

    @Autowired
    public ReservaService(ReservaRepository ry, UsuarioRepository ur, LivroRepository lr, MultaRepository mr){
        this.ry = ry;
        this.ur = ur;
        this.lr = lr;
        this.mr = mr;
    }

     // Método para um LEITOR criar uma nova reserva
    @Transactional
    public ResponseEntity<?> criarReserva(Long idUsuario, Long idLivro) {
        
        Usuario usuario = ur.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Livro livro = lr.findById(idLivro)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        
        if (ry.existsByUsuarioAndLivroAndStatusReserva(usuario, livro, StatusReserva.ATIVA)) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Você já possui uma reserva ativa para este livro.");
        }

        if(mr.existsByEmprestimoUsuarioAndStatusMulta(usuario, StatusMulta.PENDENTE)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Você possui multas pendentes no sistema.");
        }

        if (livro.getQtdDisponivel() <= 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Livro indisponível para reserva no momento.");
        }

    
        Reserva novaReserva = new Reserva();
        novaReserva.setUsuario(usuario);
        novaReserva.setLivro(livro);
        novaReserva.setDataReserva(LocalDateTime.now());
        novaReserva.setStatusReserva(StatusReserva.ATIVA);

     
        livro.setQtdDisponivel(livro.getQtdDisponivel() - 1);
        lr.save(livro);

        Reserva reservaSalva = ry.save(novaReserva);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(reservaSalva));
    }

    // Método para um LEITOR listar suas próprias reservas
    public List<ReservaDTO> encontrarReservasPorUsuario(Long idUsuario) {
        Usuario usuario = ur.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Reserva> reservas = ry.findByUsuario(usuario);

      
        return reservas.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Método para um BIBLIOTECARIO listar todas as reservas do sistema
    public List<ReservaDTO> encontrarTodasAsReservas() {
        List<Reserva> todasAsReservas = ry.findAllByOrderByDataReservaAsc();

        return todasAsReservas.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Método utilitário para converter a entidade Reserva em ReservaDTO
    private ReservaDTO convertToDTO(Reserva reserva) {
        LivroResumidoDTO livroDTO = new LivroResumidoDTO(
                reserva.getLivro().getId(),
                reserva.getLivro().getTitulo(),
                reserva.getLivro().getAutor()
        );

        UsuarioResumidoDTO usuarioDTO = new UsuarioResumidoDTO(
                reserva.getUsuario().getId(),
                reserva.getUsuario().getNome()
        );

        return new ReservaDTO(
                reserva.getId(),
                reserva.getDataReserva(),
                reserva.getStatusReserva(),
                livroDTO,
                usuarioDTO
        );
    }
    
}
