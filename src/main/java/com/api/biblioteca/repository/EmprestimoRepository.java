package com.api.biblioteca.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
}
