package com.api.biblioteca.dtos;

public class RelatorioItemDTO {
    private String titulo;
    private Long quantidade;

    // Construtor que a JPQL está chamando
    public RelatorioItemDTO(String titulo, Long quantidade) {
        this.titulo = titulo;
        this.quantidade = quantidade;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }

    
    
}
