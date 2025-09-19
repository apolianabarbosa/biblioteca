package com.api.biblioteca.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.dtos.LoginRequestDTO;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.responses.LoginResponse;
import com.api.biblioteca.service.JwtService;
import com.api.biblioteca.service.UsuarioService;

import jakarta.validation.Valid;

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
    
        Usuario authenticatedUsuario = us.autenticar(loginRequestDTO);

        String jwtToken = jwtService.generateToken(authenticatedUsuario);

        LoginResponse loginResponse = new LoginResponse()
                                            .setToken(jwtToken)
                                            .setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> cadastrarUsuario(@Valid @RequestBody Usuario u){
        return us.criarConta(u);
    }

}
