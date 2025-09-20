package com.api.biblioteca.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.model.Multa;
import com.api.biblioteca.model.Multa.StatusMulta;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.repository.UsuarioRepository;
import com.api.biblioteca.service.MultaService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/multas")
public class MultaController {

    @Autowired
    private MultaService multaService;
    
    @Autowired
    private UsuarioRepository usuarioRepository; // Usado para buscar o usuário pelo ID

    // Endpoint para pagar uma multa
    @PutMapping("/{id}/pagar")
    public ResponseEntity<Multa> pagarMulta(@PathVariable Long id) {
        Multa multaPaga = multaService.pagarMulta(id);
        return ResponseEntity.ok(multaPaga);
    }

    // Endpoint para listar todas as multas do sistema
    @GetMapping
    public ResponseEntity<List<Multa>> listarTodas() {
        List<Multa> multas = multaService.listarTodas();
        return ResponseEntity.ok(multas);
    }

    // Endpoint para buscar uma multa pelo seu ID
    @GetMapping("/{id}")
    public ResponseEntity<Multa> buscarPorId(@PathVariable Long id) {
        return multaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para listar multas por status (PENDENTE ou PAGO)
    @GetMapping("/status")
    public ResponseEntity<List<Multa>> listarPorStatus(@RequestParam("status") StatusMulta status) {
        List<Multa> multas = multaService.listarMultasPorStatus(status);
        return ResponseEntity.ok(multas);
    }
    
    // Endpoint para listar todas as multas de um usuário específico
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Multa>> listarPorUsuario(@PathVariable Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + idUsuario));
        List<Multa> multas = multaService.listarMultasPorUsuario(usuario);
        return ResponseEntity.ok(multas);
    }
}