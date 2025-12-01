package com.api.biblioteca.repository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Top 5 sem filtro
    @Query("SELECT NEW com.api.biblioteca.dtos.RelatorioItemDTO(e.livro.titulo, COUNT(e)) " +
           "FROM Emprestimo e GROUP BY e.livro.titulo ORDER BY COUNT(e) DESC")
    List<RelatorioItemDTO> findLivrosMaisEmprestados(Pageable pageable);

    // Top 5 com filtro por data e retorno do Título
    @Query("SELECT NEW com.api.biblioteca.dtos.RelatorioItemDTO(e.livro.titulo, COUNT(e)) " +
           "FROM Emprestimo e " +
           "WHERE (:startDate IS NULL OR e.dataEmprestimo >= :startDate) " +
           "AND (:endDate IS NULL OR e.dataEmprestimo <= :endDate) " +
           "GROUP BY e.livro.titulo " +
           "ORDER BY COUNT(e) DESC")
    List<RelatorioItemDTO> findLivrosMaisEmprestadosComFiltro(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable);

}
