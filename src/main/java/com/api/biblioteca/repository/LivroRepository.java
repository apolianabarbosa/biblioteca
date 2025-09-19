package com.api.biblioteca.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.Livro.StatusLivro;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long>{
    
    // Listagem global de A-Z pelo Titulo do livro
    List<Livro> findAllByOrderByTituloAsc();

    // Busca por Titulo do livro (ignorando maiúsculas/minúsculas e contendo o texto)
    List<Livro> findByTituloContainingIgnoreCase(String titulo);

    // Busca por Autor (ignorando maiúsculas/minúsculas e contendo o texto)
    List<Livro> findByAutorContainingIgnoreCase(String autor);

    // Buscar por isbn (busca extra)
    Optional<Livro> findByIsbn(String isbn);

    // Busca por Categoria 
    List<Livro> findByCategoriaContainingIgnoreCase(String categoria);

    // Busca por Status do livro 
    List<Livro> findByStatusLivro(StatusLivro statusLivro);

    // Checar isbn
    boolean existsByIsbn(String isbn);
}
