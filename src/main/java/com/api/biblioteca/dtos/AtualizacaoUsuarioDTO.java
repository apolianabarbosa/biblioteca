package com.api.biblioteca.dtos;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizacaoUsuarioDTO {
    private String nome;
    private String telefone;
    private String estado;
    private String cidade;
    private String bairro;
    private LocalDate dataNascimento;
    private String senhaAtual;
    private String novaSenha;
}
