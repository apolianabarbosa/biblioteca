package com.api.biblioteca.dtos;
import java.time.Year;

import com.api.biblioteca.model.Livro;

public class LivroDTO {
    private Long id;
    private String isbn;
    private String titulo;
    private String autor;
    private String categoria;
    private String editora;
    private Livro.StatusLivro statusLivro;
    private Year anoPublicacao;
    private Integer qtdDisponivel;

    public LivroDTO(Livro livro){
        this.id = livro.getId();
        this.isbn = livro.getIsbn();
        this.titulo = livro.getTitulo();
        this.autor = livro.getAutor();
        this.categoria = livro.getCategoria();
        this.editora = livro.getEditora();
        this.statusLivro = livro.getStatusLivro();
        this.anoPublicacao = livro.getAnoPublicacao();
        this.qtdDisponivel = livro.getQtdDisponivel();
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEditora() {
        return editora;
    }

    public void setEditora(String editora) {
        this.editora = editora;
    }

    public Livro.StatusLivro getStatusLivro() {
        return statusLivro;
    }

    public void setStatusLivro(Livro.StatusLivro statusLivro) {
        this.statusLivro = statusLivro;
    }

    public Year getAnoPublicacao() {
        return anoPublicacao;
    }

    public void setAnoPublicacao(Year anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    public Integer getQtdDisponivel() {
        return qtdDisponivel;
    }

    public void setQtdDisponivel(Integer qtdDisponivel) {
        this.qtdDisponivel = qtdDisponivel;
    }

    
}
