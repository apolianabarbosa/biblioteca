package com.api.biblioteca.model;
import java.time.Year;
import com.api.biblioteca.Validation.AnoPublicacaoValido;
import com.api.biblioteca.Validation.IsbnValido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "livros")
@Getter
@Setter
public class Livro {
    
    public enum StatusLivro{
        DISPONIVEL, RESERVADO, EMPRESTADO, INDISPONIVEL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livro")
    private Long id;

    @Column(unique = true)
    @IsbnValido
    private String isbn;

    @NotNull
    private String titulo;

    private String autor;

    @NotEmpty(message = "Categoria é obrigatória")
    private String categoria;
    
    private String editora;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] capa; 

    @Enumerated(EnumType.STRING)
    @Column(name = "status_livro")
    @NotNull(message = "Status do livro é obrigatório")
    private StatusLivro statusLivro;

    @Column(name = "ano_publicacao")
    @AnoPublicacaoValido
    private Year anoPublicacao;

    @Column(name = "qtd_total")
    @NotNull
    private Integer qtdTotal;

    @Column(name = "qtd_disponivel")
    @NotNull(message = "Quantidade disponível é obrigatória")
    private Integer qtdDisponivel;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String descricao;
}
