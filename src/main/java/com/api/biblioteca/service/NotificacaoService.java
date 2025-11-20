package com.api.biblioteca.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.biblioteca.dtos.NotificacaoDTO;
import com.api.biblioteca.model.Notificacao;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.repository.NotificacaoRepository;

@Service
public class NotificacaoService {
    
    @Autowired
    private NotificacaoRepository nr;

    @Autowired
    private UsuarioService us;

    // Método CRIAR notificação
    public void criarNotificacao(Usuario destinatario, String mensagem){
        if(destinatario == null || mensagem ==  null || mensagem.isBlank()){
            return;
        }

        Notificacao notificacao = new Notificacao(destinatario, mensagem);
        nr.save(notificacao);
    }

    //Método LISTAR notificações do usuário logado
    public List<NotificacaoDTO> getMinhasNotificacoes(){
        Usuario usuarioLogado = getUsuarioLogado();

        List<Notificacao> notificacoes = nr.findByDestinatarioOrderByDataCriacaoDesc(usuarioLogado);

        return notificacoes.stream().map(NotificacaoDTO::new).collect(Collectors.toList());

    }

    public NotificacaoDTO marcarComoLida(Long notificacaoId){
        Usuario usuarioLogado = getUsuarioLogado();

        Notificacao notificacao = nr.findById(notificacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
    
        if (!notificacao.getDestinatario().getId().equals(usuarioLogado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }

        notificacao.setLida(true);
        nr.save(notificacao);

        return new NotificacaoDTO(notificacao);
    }

    public long getContagemNaoLidas(){
        Usuario usuarioLogado = getUsuarioLogado();

        return nr.countByDestinatarioAndLidaFalse(usuarioLogado);
    }

    private Usuario getUsuarioLogado(){
        return us.getUsuarioLogado()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Usuário não autenticado"));
    }
}
