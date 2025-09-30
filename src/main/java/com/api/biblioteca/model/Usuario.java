 package com.api.biblioteca.model;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.api.biblioteca.Validation.CpfValido;
import com.api.biblioteca.Validation.DataNascimentoValida;
import com.api.biblioteca.Validation.EmailValido;
import com.api.biblioteca.Validation.SenhaValida;
import com.api.biblioteca.Validation.TelefoneValido;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter

public class Usuario implements UserDetails{

    // Enum para Sexo
    public enum Sexo {
        MASCULINO, FEMININO, OUTRO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O tipo do usuário não pode ser nulo")
    private UsuarioRole role;

    @NotEmpty(message = "Nome é obrigatório")
    private String nome;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;
    
    @Column(unique = true)
    @NotEmpty(message = "CPF é obrigatório")
    @CpfValido
    private String cpf;

    @Column(unique = true)
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

    @Column(name = "data_cadastro", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dataCadastro;

    @PrePersist
    public void prePersist() {
        this.dataCadastro = LocalDateTime.now();
    }

    @Column(name = "data_nascimento")
    @NotNull(message = "Incira a Data de Nascimento")
    @DataNascimentoValida
    private LocalDate dataNascimento;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;

     // Métodos da interface UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.role == UsuarioRole.BIBLIOTECARIO){
            return List.of(new SimpleGrantedAuthority("ROLE_BIBLIOTECARIO"), new SimpleGrantedAuthority("ROLE_LEITOR"));
        }else{
            return List.of(new SimpleGrantedAuthority("ROLE_LEITOR"));
        }
        
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email; 
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; 
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return true; 
    }

    @Override
    public boolean isEnabled() {
        return true; 
    }

    
}