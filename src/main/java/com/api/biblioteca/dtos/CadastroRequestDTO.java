// Crie este novo arquivo: CadastroRequestDTO.java
package com.api.biblioteca.dtos; // Ou o pacote onde seus DTOs estão

import com.api.biblioteca.Validation.CpfValido;
import com.api.biblioteca.Validation.DataNascimentoValida;
import com.api.biblioteca.Validation.EmailValido;
import com.api.biblioteca.Validation.SenhaValida;
import com.api.biblioteca.Validation.TelefoneValido;
import com.api.biblioteca.model.Usuario.Sexo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;


// Usamos getters para que o Spring possa ler os dados
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

    // Crie os Getters para todos os campos. 
    // A maioria das IDEs pode gerar isso automaticamente (clique com o botão direito -> Source Action -> Generate Getters and Setters).
    // Não precisamos de Setters, pois os dados só virão do JSON.

    public String getNome() { return nome; }
    public Sexo getSexo() { return sexo; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public String getTelefone() { return telefone; }
    public String getEstado() { return estado; }
    public String getCidade() { return cidade; }
    public String getBairro() { return bairro; }
    public LocalDate getDataNascimento() { return dataNascimento; }
}