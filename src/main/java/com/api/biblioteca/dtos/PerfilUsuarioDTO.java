package com.api.biblioteca.dtos;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.model.Usuario.Sexo;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class PerfilUsuarioDTO {
    
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private Sexo sexo;
    private LocalDate dataNascimento;
    private String estado;
    private String cidade;
    private String bairro;

    public PerfilUsuarioDTO(Usuario usuario) {
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.cpf = usuario.getCpf();
        this.telefone = usuario.getTelefone();
        this.sexo = usuario.getSexo();
        this.dataNascimento = usuario.getDataNascimento();
        this.estado = usuario.getEstado();
        this.cidade = usuario.getCidade();
        this.bairro = usuario.getBairro();
    }
}
