package com.api.biblioteca.dtos;

import java.time.LocalDateTime;

import com.api.biblioteca.model.Notificacao;

import lombok.Data;

@Data
public class NotificacaoDTO {
    private Long id;
    private String mensagem;
    private boolean lida;
    private LocalDateTime dataCriacao;
    private Long id_usuario;

    public NotificacaoDTO(Notificacao notificacao) {
        this.id = notificacao.getId();
        this.mensagem = notificacao.getMensagem();
        this.lida = notificacao.isLida();
        this.dataCriacao = notificacao.getDataCriacao();
        this.id_usuario = notificacao.getDestinatario().getId();
    }
    
}
