package com.api.biblioteca.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.api.biblioteca.dtos.CadastroRequestDTO; 
import com.api.biblioteca.dtos.LoginRequestDTO;

import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.model.UsuarioRole;
import com.api.biblioteca.responses.LoginResponse;
import com.api.biblioteca.service.JwtService;
import com.api.biblioteca.service.UsuarioService;
import jakarta.validation.Valid;
record SenhaEsquecidaDTO(String email) {}
record RedefinirSenhaDTO(String email, String novaSenha) {}

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final UsuarioService us;

    public AuthenticationController(JwtService jwtService, UsuarioService us){
        this.jwtService = jwtService;
        this.us = us;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticarUsuario(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        // ... seu método de login continua igual ...
        Usuario authenticatedUsuario = us.autenticar(loginRequestDTO);
        String jwtToken = jwtService.generateToken(authenticatedUsuario);
        LoginResponse loginResponse = new LoginResponse()
                                            .setToken(jwtToken)
                                            .setExpiresIn(jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }

    // <-- MUDANÇA 2: O método agora recebe CadastroRequestDTO
    @PostMapping("/signup")
    public ResponseEntity<?> cadastrarUsuario(@Valid @RequestBody CadastroRequestDTO dto){
        return us.criarConta(dto);
    }

    @PostMapping("/recuperarSenha")
    public ResponseEntity<String> verificarEmail(@RequestBody SenhaEsquecidaDTO dto) {
        try {
            us.verificarEmail(dto.email());
            return ResponseEntity.ok("E-mail válido. Você pode redefinir sua senha.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/novaSenha")
    public ResponseEntity<String> redefinirSenha(@RequestBody RedefinirSenhaDTO dto) {
        try {
            us.redefinirSenha(dto.email(), dto.novaSenha());
            return ResponseEntity.ok("Senha redefinida com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}