package com.api.biblioteca.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.model.UsuarioRole;



@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    
    // MÉTODOS BASE //
    // Checar email (já existente)
    boolean existsByEmail(String email);

    // Checar cpf
    boolean existsByCpf(String cpf);

    // Busca por email
    Optional<Usuario> findByEmail(String email);

    // MÉTODOS PARA VISÃO ADM - BIBLIOTECARIO
    // Listagem Global de todos usuarios no sistema de A-Z
    List<Usuario> findAllByOrderByNomeAsc();

    // Busca dinamica pelo nome do usuario
    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    // Filtro por tipo de usuario de A-Z
    List<Usuario> findByRoleOrderByNomeAsc(UsuarioRole role);


}