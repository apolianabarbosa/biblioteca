package com.api.biblioteca.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.dtos.NotificacaoDTO;
import com.api.biblioteca.service.NotificacaoService;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {
    
    @Autowired
    private NotificacaoService  ns;

    // Listar notificações 
    @GetMapping("/minhas")
    @PreAuthorize("hasRole('LEITOR')")
    public ResponseEntity<List<NotificacaoDTO>> getMinhas(){
        List<NotificacaoDTO> notificaoes = ns.getMinhasNotificacoes();
        return ResponseEntity.ok(notificaoes);
    }

    // Contagem de notificações
    @GetMapping("/naoLidas/contagem")
    @PreAuthorize("hasRole('LEITOR')")
    public ResponseEntity<Long> getContagemNaoLidas(){
        long contagem = ns.getContagemNaoLidas();
        return ResponseEntity.ok(contagem);
    }

    // Marcar como lida
    @PutMapping("/{id}/lida")
    @PreAuthorize("hasRole('LEITOR')")
    public ResponseEntity<NotificacaoDTO> marcarComoLida(@PathVariable Long id){
        NotificacaoDTO notificacaoAtualizada = ns.marcarComoLida(id);
        return ResponseEntity.ok(notificacaoAtualizada);
    }


}
