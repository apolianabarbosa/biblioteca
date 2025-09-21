package com.api.biblioteca.dtos;

import java.time.LocalDateTime;

public class EmprestimoRequestDTO {

    private Long idUsuario;
    private Long idLivro;
    private LocalDateTime dataPrevistaDevolucao;

    public EmprestimoRequestDTO() {
    }

    public EmprestimoRequestDTO(Long idUsuario, Long idLivro, LocalDateTime dataPrevistaDevolucao) {
        this.idUsuario = idUsuario;
        this.idLivro = idLivro;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdLivro() {
        return idLivro;
    }

    public void setIdLivro(Long idLivro) {
        this.idLivro = idLivro;
    }

    public LocalDateTime getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public void setDataPrevistaDevolucao(LocalDateTime dataPrevistaDevolucao) {
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
    }
}