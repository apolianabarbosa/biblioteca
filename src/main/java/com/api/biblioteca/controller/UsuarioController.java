package com.api.biblioteca.controller;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.api.biblioteca.dtos.UsuarioDTO;
import com.api.biblioteca.model.RespostaModel;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.service.JwtService;
import com.api.biblioteca.service.UsuarioService;
import com.api.biblioteca.dtos.PerfilUsuarioDTO; // Importação do DTO de perfil

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService us;
    private final JwtService jwtService;

    @Autowired
    public UsuarioController(UsuarioService us, JwtService jwtService) {
        this.us = us;
        this.jwtService = jwtService;
    }
    
    // ROTAS PUBLICAS
    @GetMapping("/bem-vinda")
    public String rota(){
        return "api biblioteca funcionando";
    }

    // ROTAS PROTEGIDA 
    @GetMapping("/meuPerfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMeuPerfil(){
        Optional<Usuario> usuarioOpt = us.getUsuarioLogado();

        if(usuarioOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new RespostaModel("Nenhum usuário autenticado."));
        }

        // Convertendo a entidade Usuario para o DTO Perfil
        PerfilUsuarioDTO perfilDTO = new PerfilUsuarioDTO(usuarioOpt.get());

        // Retorna o DTO com os dados necessários
        return ResponseEntity.ok(perfilDTO);
    }

    // Rota atualizar dados 
    @PutMapping("/atualizarDados")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<?> atualizarMeuPerfil(@RequestBody Map<String, Object> dadosAtualizados) {
        return us.atualizarDadosUsuario(dadosAtualizados);
    }

}
