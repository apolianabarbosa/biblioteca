package com.api.biblioteca.controller;

import java.io.IOException; // ✅ Novo import para tratar exceções de arquivo
import java.time.Year; // ✅ Novo import para o tipo 'Year'
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile; // ✅ Novo import para upload de arquivo

import com.api.biblioteca.dtos.LivroDTO;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.Livro.StatusLivro;
import com.api.biblioteca.model.RespostaModel;
import com.api.biblioteca.service.LivroService;

@RequestMapping("/livros")
@RestController
public class LivroController {

    private final LivroService ls;

    public LivroController(LivroService ls) {
        this.ls = ls;
    }

    // ✅ ========================================================================
    // ✅ MÉTODO DE CADASTRO ATUALIZADO PARA ACEITAR UPLOAD DE ARQUIVO
    // ✅ ========================================================================
    @PostMapping("/cadastrar")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<?> cadastrarLivro(
            // Não usamos mais @RequestBody. Cada campo do formulário vem como um @RequestParam.
            @RequestParam("titulo") String titulo,
            @RequestParam("autor") String autor,
            @RequestParam("isbn") String isbn,
            @RequestParam("categoria") String categoria,
            @RequestParam("editora") String editora,
            @RequestParam("anoPublicacao") Integer anoPublicacao,
            @RequestParam("qtdTotal") Integer qtdTotal,
            @RequestParam("descricao") String descricao,
            // O arquivo da capa é opcional (required = false)
            @RequestParam(value = "capa", required = false) MultipartFile capaFile
    ) {
        // 1. Criamos um novo objeto Livro a partir dos parâmetros recebidos.
        Livro novoLivro = new Livro();
        novoLivro.setTitulo(titulo);
        novoLivro.setAutor(autor);
        novoLivro.setIsbn(isbn);
        novoLivro.setCategoria(categoria);
        novoLivro.setEditora(editora);
        novoLivro.setAnoPublicacao(Year.of(anoPublicacao));
        novoLivro.setQtdTotal(qtdTotal);
        novoLivro.setDescricao(descricao);
        novoLivro.setStatusLivro(StatusLivro.DISPONIVEL); // Define um status inicial

        // 2. Tratamos o arquivo da capa.
        try {
            // Se um arquivo foi enviado e não está vazio...
            if (capaFile != null && !capaFile.isEmpty()) {
                // ...convertemos ele para um array de bytes e o definimos no nosso objeto.
                novoLivro.setCapa(capaFile.getBytes());
            }
        } catch (IOException e) {
            // Se der erro ao ler o arquivo, retornamos um erro para o cliente.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new RespostaModel("Erro ao processar o arquivo da capa."));
        }

        // 3. Enviamos o objeto Livro, agora completo, para o serviço, que já sabe como salvá-lo.
        return ls.cadastrarLivro(novoLivro);
    }
    
    // O restante do controller permanece o mesmo...

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

    @GetMapping
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public List<LivroDTO> listarTodosLivros() {
        return ls.listarTodos();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<?> getLivroPorId(@PathVariable Long id) {
        Optional<Livro> livroOpt = ls.buscarPorId(id);
        if (livroOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RespostaModel("Livro com o ID " + id + " não encontrado."));
        }
        LivroDTO dto = new LivroDTO(livroOpt.get());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/capa")
    public ResponseEntity<byte[]> getCapaDoLivro(@PathVariable Long id) {
        Optional<Livro> livroOpt = ls.buscarPorId(id);
        if (livroOpt.isEmpty() || livroOpt.get().getCapa() == null) {
            return ResponseEntity.notFound().build();
        }
        Livro livro = livroOpt.get();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(livro.getCapa());
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<List<LivroDTO>> buscarGeral(@RequestParam("termo") String termo) {
        List<LivroDTO> livrosEncontrados = ls.buscarLivrosPorTermoGeral(termo);
        if (livrosEncontrados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(livrosEncontrados);
    }
}