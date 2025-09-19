package com.api.biblioteca.controller;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.api.biblioteca.dtos.LivroDTO;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.RespostaModel;
import com.api.biblioteca.model.Livro.StatusLivro;
import com.api.biblioteca.service.LivroService;

@RequestMapping("/livros")
@RestController
public class LivroController {

    private final LivroService ls;

    public LivroController(LivroService ls) {
        this.ls = ls;
    }

    // MÉTODOS DE CADASTRO E ATUALIZAÇÃO (Acesso de Bibliotecário)

    @PostMapping("/cadatrar")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<?> cadastrarLivro(@RequestBody Livro livro) {
        return ls.cadastrarLivro(livro);
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<?> atualizarLivro(@PathVariable Long id, @RequestBody Map<String, Object> dadosAtualizados) {
        return ls.atualizarLivroParcial(id, dadosAtualizados);
    }

    @DeleteMapping("/remover/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<RespostaModel> deletarLivro(@PathVariable Long id) {
        return ls.deletarLivro(id);
    }

    // MÉTODOS DE BUSCA E LISTAGEM (Acesso de Leitor e Bibliotecário)

    @GetMapping
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public List<LivroDTO> listarTodosLivros() {
        return ls.listarTodos();
    }

    @GetMapping("/buscar/titulo/{titulo}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<?> buscarPorTitulo(@PathVariable String titulo) {
        return ls.buscarPorTitulo(titulo);
    }

    @GetMapping("/buscar/autor/{autor}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<?> buscarPorAutor(@PathVariable String autor) {
        return ls.buscarPorAutor(autor);
    }

    @GetMapping("/buscar/isbn/{isbn}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<?> buscarPorIsbn(@PathVariable String isbn) {
        return ls.buscarPorIsbn(isbn);
    }

    @GetMapping("/filtrar/categoria")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<?> buscarPorCategoria(@RequestParam("categoria") String categoria) {
        return ls.buscarPorCategoria(categoria);
    }

    @GetMapping("/filtrar/statusLivro")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<?> filtrarPorStatusLivro(@RequestParam("status") StatusLivro status) {
        return ls.buscarPorStatusLivro(status);
    }
}