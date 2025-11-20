package com.api.biblioteca.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@Data@NoArgsConstructor
public class Notificacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario destinatario;

    @Column(nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private boolean lida = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public Notificacao(Usuario destinatario, String mensagem){
        this.destinatario = destinatario;
        this.mensagem = mensagem;
    }

}
