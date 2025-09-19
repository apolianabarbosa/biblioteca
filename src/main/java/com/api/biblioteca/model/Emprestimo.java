package com.api.biblioteca.model;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "emprestimos")
@Getter
@Setter
public class Emprestimo {
    
    public enum StatusEmprestimo{
        ATIVO, FINALIZADO, ATRASADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emprestimo")
    private Long id;

    @Column(name = "data_emprestimo", updatable = false)
    private LocalDateTime dataEmprestimo;


    @Column(name = "data_prevista_devolucao", updatable = false)
    private LocalDateTime dataPrevistaDevolucao;

    @PrePersist
    public void prePersist(){
        this.dataEmprestimo = LocalDateTime.now();
        this.dataPrevistaDevolucao = this.dataEmprestimo.plusDays(15);
    }


    @Enumerated(EnumType.STRING)
    @Column(name = "status_emprestimo")
    private StatusEmprestimo statusEmprestimo;

    @ManyToOne
    @JoinColumn(name = "fk_id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "fk_id_livro", nullable = false)
    private Livro livro;

}