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
record RedefinirSenhaDTO(String token, String novaSenha) {}

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

    @PostMapping("/forgot-password")
    public ResponseEntity<String> solicitarRedefinicao(@RequestBody SenhaEsquecidaDTO dto) {
        try {
            us.solicitarRedefinicao(dto.email());
            return ResponseEntity.ok("Se o e-mail existir em nosso sistema, um link de redefinição será enviado.");
        } catch (RuntimeException e) {
            // Retornamos uma mensagem genérica por segurança, para não confirmar se um e-mail existe ou não
            return ResponseEntity.ok("Se o e-mail existir em nosso sistema, um link de redefinição será enviado.");
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<String> redefinirSenhaComToken(@RequestBody RedefinirSenhaDTO dto) {
        try {
            us.redefinirSenhaComToken(dto.token(), dto.novaSenha());
            return ResponseEntity.ok("Senha redefinida com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}