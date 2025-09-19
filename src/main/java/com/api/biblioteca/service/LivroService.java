package com.api.biblioteca.service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.api.biblioteca.dtos.LivroDTO;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.Livro.StatusLivro;
import com.api.biblioteca.model.RespostaModel;
import com.api.biblioteca.repository.LivroRepository;

@Service
public class LivroService {
    private final LivroRepository lr;

    @Autowired
    public LivroService(LivroRepository lr){
        this.lr = lr;
    }

// MÉTODOS DE CADASTRO E ATUALIZAÇÃO (Acesso de Bibliotecário)

    // Método de cadastro de livro
    public ResponseEntity<?> cadastrarLivro(Livro livro){
        
        if(lr.existsByIsbn(livro.getIsbn())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new RespostaModel("ISBEN já usado anteriormente"));
        }

        livro.setQtdDisponivel(livro.getQtdTotal());

        Livro livroSalvo = lr.save(livro);

        LivroDTO dto = new LivroDTO(livroSalvo);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Método para ATUALIZAR um livro existente
    public ResponseEntity<?> atualizarLivro(Long id, Livro dadosAtualizados) {
        Optional<Livro> livroOpt = lr.findById(id);

        if (livroOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new RespostaModel("Livro com o ID " + id + " não encontrado."));
        }

        Livro livroExistente = livroOpt.get();

        Optional<Livro> livroMesmoIsbn = lr.findByIsbn(dadosAtualizados.getIsbn());

        if(livroMesmoIsbn.isPresent() && !livroMesmoIsbn.get().getId().equals(id)){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new RespostaModel("O ISBN " + dadosAtualizados.getIsbn() + "já está em uso por outro livro."));
        }

        int livrosEmprestados = livroExistente.getQtdTotal() - livroExistente.getQtdDisponivel();
        int novaQtdDisponivel = dadosAtualizados.getQtdTotal() - livrosEmprestados;

        if(novaQtdDisponivel < 0 ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new RespostaModel("A nova quantidade total não pode ser menor que a quantidade de livros emprestados."));
        }

        livroExistente.setQtdDisponivel(novaQtdDisponivel);
        livroExistente.setIsbn(dadosAtualizados.getIsbn());
        livroExistente.setTitulo(dadosAtualizados.getTitulo());
        livroExistente.setAutor(dadosAtualizados.getAutor());
        livroExistente.setCategoria(dadosAtualizados.getCategoria());
        livroExistente.setEditora(dadosAtualizados.getEditora());
        livroExistente.setAnoPublicacao(dadosAtualizados.getAnoPublicacao());
        livroExistente.setDescricao(dadosAtualizados.getDescricao());
        livroExistente.setQtdTotal(dadosAtualizados.getQtdTotal());
        livroExistente.setCapa(dadosAtualizados.getCapa());

        Livro livroSalvo = lr.save(livroExistente);
        LivroDTO dto = new LivroDTO(livroSalvo);

        return ResponseEntity.ok(dto);
    }

    // Método para DELETAR um livro
    public ResponseEntity<RespostaModel> deletarLivro(Long id) {

        if (!lr.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new RespostaModel("Livro com o ID " + id + " não encontrado."));
        }

        lr.deleteById(id);
        return ResponseEntity.ok(new RespostaModel("Livro deletado com sucesso."));
    }

    // MÉTODOS DE BUSCA E LISTAGEM (Acesso de Bibliotecário e Leitor)

    // Método para LISTAR todos os livros em ordem alfabética
    public List<LivroDTO> listarTodos() {
        List<Livro> livros = lr.findAllByOrderByTituloAsc();
        // Converte a lista de Livro para uma lista de LivroDTO
        return livros.stream().map(LivroDTO::new).collect(Collectors.toList());
    }

    // Método para BUSCAR livros por TÍTULO
    public ResponseEntity<?> buscarPorTitulo(String titulo) {

        List<Livro> livros = lr.findByTituloContainingIgnoreCase(titulo);

        if (livros.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new RespostaModel("Nenhum livro encontrado com o Título: " + titulo));
        }

        List<LivroDTO> dto = livros.stream().map(LivroDTO::new).collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }
    
    // Método para BUSCAR livros por Autor
    public ResponseEntity<?> buscarPorAutor(String autor){

        List<Livro> livros = lr.findByAutorContainingIgnoreCase(autor);

        if(livros.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new RespostaModel("Nenhum livro encontrado com o Autor: " + autor));
        }

        List<LivroDTO> dto = livros.stream().map(LivroDTO::new).collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    // Método para BUSCAR livros por ISBN
    public ResponseEntity<?> buscarPorIsbn(String isbn){

       Optional<Livro> livroOpt = lr.findByIsbn(isbn);

       if(livroOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new RespostaModel("Nenhum livro encontrado para o ISBN: " + isbn));
        }

        LivroDTO dto = new LivroDTO(livroOpt.get());

        return ResponseEntity.ok(dto);
    }

    // Método para BUSCAR livros por Categoria
    public ResponseEntity<?> buscarPorCategoria(String categoria){

        List<Livro> livros = lr.findByCategoriaContainingIgnoreCase(categoria);

        if(livros.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new RespostaModel("Nenhum livro encontrado para a Categoria: " + categoria));
        }

        List<LivroDTO> dto = livros.stream().map(LivroDTO::new).collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    // Método para BUSCAR livros por Status do Livro
    public ResponseEntity<?> buscarPorStatusLivro(StatusLivro statusLivro){

        List<Livro> livros = lr.findByStatusLivro(statusLivro);

        if(livros.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new RespostaModel("Nenhum livro encontrado para o Status selecionado: " + statusLivro));
        }

        List<LivroDTO> dto = livros.stream().map(LivroDTO::new).collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

}

