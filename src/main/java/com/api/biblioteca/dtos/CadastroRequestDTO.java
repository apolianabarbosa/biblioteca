// Crie este novo arquivo: CadastroRequestDTO.java
package com.api.biblioteca.dtos; // Ou o pacote onde seus DTOs estão

import java.time.LocalDate;

import com.api.biblioteca.Validation.CpfValido;
import com.api.biblioteca.Validation.DataNascimentoValida;
import com.api.biblioteca.Validation.EmailValido;
import com.api.biblioteca.Validation.SenhaValida;
import com.api.biblioteca.Validation.TelefoneValido;
import com.api.biblioteca.model.Usuario.Sexo;
import com.api.biblioteca.model.UsuarioRole;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;


@Getter
public class CadastroRequestDTO {

    @NotEmpty(message = "Nome é obrigatório")
    private String nome;

    private Sexo sexo;
    
    @NotEmpty(message = "CPF é obrigatório")
    @CpfValido
    private String cpf;

    @NotEmpty(message = "E-mail é obrigatório")
    @EmailValido
    private String email;

    @NotEmpty(message = "Senha é obrigatória")
    @SenhaValida
    private String senha;

    @NotEmpty(message = "Telefone é obrigatório")
    @TelefoneValido
    private String telefone;

    @NotEmpty(message = "Selecione o Estado")
    private String estado;

    @NotEmpty(message = "Selecione a Cidade")
    private String cidade;

    @NotEmpty(message = "Bairro é obrigatório")
    private String bairro;

    @NotNull(message = "Insira a Data de Nascimento")
    @DataNascimentoValida
    private LocalDate dataNascimento;

    @NotNull(message = "O tipo de usuário (role) não pode ser nulo")
    private UsuarioRole role;

    private String codigoAdministrativo;
}