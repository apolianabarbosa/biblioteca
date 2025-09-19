package com.api.biblioteca.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.dtos.UsuarioDTO;
import com.api.biblioteca.model.UsuarioRole;
import com.api.biblioteca.service.UsuarioService;

@RequestMapping("/admin")
@RestController
public class AdminController {
    
    private final UsuarioService us;

    public AdminController(UsuarioService us){
        this.us = us;
    }

    // Rota listagem gloobal de usuario
    @GetMapping("/listarUsuarios")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<List<UsuarioDTO>> listarTodosUsuario(){
        List<UsuarioDTO> usuarios = us.listaTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    // Rota de busca pelo nome do usuario
    @GetMapping("/buscar")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<?> buscarUsuarioPorNome(@RequestParam String nome){
        return us.buscarUsuarioPorNome(nome);
    }

    // Rota de filtro por tipo de usuário
    @GetMapping("/filtrar/{role}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<?> filtrarUsuarioPorTipo(@PathVariable UsuarioRole role){
        return us.filtrarUsuarioPorRole(role);
    }
}
