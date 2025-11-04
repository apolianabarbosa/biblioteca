package com.api.biblioteca.controller;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.dtos.MultaDTO;
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
    @PutMapping("/pagar/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
     public ResponseEntity<MultaDTO> pagarMulta(@PathVariable Long id) { 
        Multa multaPaga = multaService.pagarMulta(id);
        return ResponseEntity.ok(multaService.toDTO(multaPaga));
    }

    // Endpoint para listar todas as multas do sistema
    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<List<MultaDTO>> listarTodas() { 
        List<Multa> multas = multaService.listarTodas();
        List<MultaDTO> dtos = multas.stream()
                .map(multaService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Endpoint para buscar uma multa pelo seu ID
    @GetMapping("/buscar/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<MultaDTO> buscarPorId(@PathVariable Long id) {
        return multaService.buscarPorId(id)
                .map(multaService::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para listar multas por status (PENDENTE ou PAGO)
    @GetMapping("/filtrar/statusMulta")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
     public ResponseEntity<List<MultaDTO>> listarPorStatus(@RequestParam("status") StatusMulta status) { 
        List<Multa> multas = multaService.listarMultasPorStatus(status);
        List<MultaDTO> dtos = multas.stream()
                .map(multaService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }


    // Endpoint para listar todas as multas de um usuário específico
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasRole('LEITOR')")
    public ResponseEntity<List<MultaDTO>> listarPorUsuario(@PathVariable Long idUsuario) { 
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + idUsuario));
        List<Multa> multas = multaService.listarMultasPorUsuario(usuario);
        List<MultaDTO> dtos = multas.stream()
                .map(multaService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}