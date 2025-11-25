package com.api.biblioteca.repository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.api.biblioteca.dtos.RelatorioItemDTO;
import com.api.biblioteca.model.Emprestimo;
import com.api.biblioteca.model.Emprestimo.StatusEmprestimo;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.Usuario;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long>{
    
     // MÉTODOS BASES
    // Listagem  Global Ordenada (todos empréstimos do sistema)
    List<Emprestimo> findAllByOrderByDataEmprestimoDesc();

    // Busca pelo status do emprestimo
    List<Emprestimo> findByStatusEmprestimo(StatusEmprestimo statusEmprestimo);

    // Busca pelo status do emprestimo
    List<Emprestimo> findByLivro(Livro livro);

    // MÉTODOS - LEITOR
    // Retorna todos os empréstimos de um usuário específico
    List<Emprestimo> findByUsuario(Usuario usuario);

    // Busca empréstimos de um usuário por status 
    List<Emprestimo> findByUsuarioAndStatusEmprestimo(Usuario usuario, StatusEmprestimo statusEmprestimo);

    // Método para identificar a existência de emprestimos em biblioteca service
    boolean existsByLivroId(Long livroId);

    long countByLivroId(Long livroId);

    long countByStatusEmprestimoNot(Emprestimo.StatusEmprestimo status);

    long countByLivroAndStatusEmprestimoIn(Livro livro, List<StatusEmprestimo> statusEmprestimo);
    
    boolean existsByLivroIdAndStatusEmprestimoNot(Long livroId, StatusEmprestimo statusEmprestimo);

    boolean existsByUsuarioAndStatusEmprestimoIn(Usuario usuario, List<StatusEmprestimo> status);

    boolean existsByLivroIdAndStatusEmprestimoIn(Long livroId, List<StatusEmprestimo> status);

    @Query("SELECT new com.api.biblioteca.dtos.RelatorioItemDTO(l.titulo, COUNT(e)) " +
           "FROM Emprestimo e JOIN e.livro l " +
           "GROUP BY l.titulo " +
           "ORDER BY COUNT(e) DESC")
    List<RelatorioItemDTO> findLivrosMaisEmprestados(org.springframework.data.domain.Pageable limit);
}
