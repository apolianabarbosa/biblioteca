package com.api.biblioteca.dtos;

import com.api.biblioteca.model.Livro;
import java.time.Year;

// Este DTO (Data Transfer Object) transporta os dados do livro para o frontend,
// incluindo a informação extra se o usuário já tem uma reserva.
public record DetalhesLivroResponseDTO(
    Long id,
    String titulo,
    String autor,
    String isbn,
    String categoria,
    String editora,
    Year anoPublicacao,
    String descricao,
    byte[] capa,
    int qtdDisponivel,
    int qtdTotal,
    Livro.StatusLivro statusLivro,
    boolean usuarioJaReservou // Novo campo para controlar o botão no frontend
) {
    // Construtor para facilitar a conversão da entidade Livro para este DTO
    public DetalhesLivroResponseDTO(Livro livro, boolean usuarioJaReservou) {
        this(
            livro.getId(),
            livro.getTitulo(),
            livro.getAutor(),
            livro.getIsbn(),
            livro.getCategoria(),
            livro.getEditora(),
            livro.getAnoPublicacao(),
            livro.getDescricao(),
            livro.getCapa(),
            livro.getQtdDisponivel(),
            livro.getQtdTotal(),
            livro.getStatusLivro(),
            usuarioJaReservou // Atribui o valor calculado
        );
    }
}

