package com.api.biblioteca.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.dtos.CadastroRequestDTO; // <-- MUDANÇA 1: Importe o novo DTO
import com.api.biblioteca.dtos.LoginRequestDTO;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.model.UsuarioRole;
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
        // Agora o System.out.println vai aparecer!
        System.out.println(">>> CHEGOU NO AuthenticationController.cadastrarUsuario COM DTO <<<");

        // 3. Convertemos o DTO para a nossa entidade Usuario
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dto.getNome());
        novoUsuario.setSexo(dto.getSexo());
        novoUsuario.setCpf(dto.getCpf());
        novoUsuario.setEmail(dto.getEmail());
        novoUsuario.setSenha(dto.getSenha());
        novoUsuario.setTelefone(dto.getTelefone());
        novoUsuario.setEstado(dto.getEstado());
        novoUsuario.setCidade(dto.getCidade());
        novoUsuario.setBairro(dto.getBairro());
        novoUsuario.setDataNascimento(dto.getDataNascimento());
        
        // 4. E definimos o role com segurança aqui
        novoUsuario.setRole(UsuarioRole.LEITOR);

        // 5. Finalmente, enviamos a entidade completa e controlada para o serviço
        return us.criarConta(novoUsuario);
    }
}