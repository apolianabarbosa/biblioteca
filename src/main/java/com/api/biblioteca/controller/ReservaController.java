package com.api.biblioteca.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.api.biblioteca.dtos.ReservaDTO;
import com.api.biblioteca.model.RespostaModel;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.service.ReservaService;
import com.api.biblioteca.service.UsuarioService;

record CriarReservaRequest(Long idLivro) {}

@RequestMapping("/reservas")
@RestController
public class ReservaController {
    
    private final ReservaService rs;
    private final UsuarioService us;

    public ReservaController(ReservaService rs, UsuarioService us){
        this.rs = rs;
        this.us = us;
    }

    // MÉTODOS
    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('LEITOR')")
    public ResponseEntity<?> criarReserva(@RequestBody CriarReservaRequest request) {
        Optional<Usuario> usuarioOpt = us.getUsuarioLogado();

        if(usuarioOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new RespostaModel("Nenhum usuário autenticado."));
        }

        Long idUsuarioLogado = usuarioOpt.get().getId();
        return rs.criarReserva(idUsuarioLogado, request.idLivro());
    }

    @GetMapping("/listarTodas")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<?> getTodasAsReservas() {
        List<ReservaDTO> reservas = rs.encontrarTodasAsReservas();
    
        if (reservas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                        .body(new RespostaModel("Nenhuma reserva encontrada no sistema."));
        }

        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('LEITOR')")
    public ResponseEntity<?> getMinhasReservas() {
        Optional<Usuario> usuarioOpt = us.getUsuarioLogado();

        if(usuarioOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new RespostaModel("Nenhum usuário autenticado."));
        }

        Long idUsuarioLogado = usuarioOpt.get().getId();
        List<ReservaDTO> usuarioReservas = rs.encontrarReservasPorUsuario(idUsuarioLogado);

        if(usuarioReservas.isEmpty()){
            return ResponseEntity.ok("Você não possui nenhuma reserva.");
        }

        return ResponseEntity.ok(usuarioReservas);
    }

}
