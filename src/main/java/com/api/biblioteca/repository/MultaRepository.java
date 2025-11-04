package com.api.biblioteca.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.api.biblioteca.model.Multa;
import com.api.biblioteca.model.Multa.StatusMulta;
import com.api.biblioteca.model.Usuario;

@Repository
public interface MultaRepository extends JpaRepository<Multa, Long>{
    
    // MÉTODO - BIBLIOTECARIO
    // Busca por multas pelo usuario comum
    List<Multa> findByEmprestimoUsuario(Usuario usuario);

    // Busca por status da multa
    List<Multa> findByStatusMulta(StatusMulta statusMulta);

    // MÉTODO - LEITOR
    // Busca multas de um usuário com um status específico
    List<Multa> findByEmprestimoUsuarioAndStatusMulta(Usuario usuario, StatusMulta statusMulta);

    boolean existsByEmprestimoUsuarioAndStatusMulta(Usuario usuario, StatusMulta statusMulta);
}
