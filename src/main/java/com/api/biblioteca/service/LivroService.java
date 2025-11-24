package com.api.biblioteca.service;
import java.io.IOException;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.api.biblioteca.dtos.DetalhesLivroResponseDTO;
import com.api.biblioteca.dtos.LivroDTO;
import com.api.biblioteca.model.Emprestimo;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.Livro.StatusLivro;
import com.api.biblioteca.model.Reserva;
import com.api.biblioteca.model.RespostaModel;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.repository.EmprestimoRepository;
import com.api.biblioteca.repository.LivroRepository;
import com.api.biblioteca.repository.ReservaRepository;

@Service
public class LivroService {
    private final LivroRepository lr;
    private final EmprestimoRepository emprestimoRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioService us;

    @Autowired
    public LivroService(LivroRepository lr, EmprestimoRepository emprestimoRepository, ReservaRepository reservaRepository, UsuarioService us){
        this.lr = lr;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
        this.us = us;
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
    public ResponseEntity<?> atualizarLivroParcial(Long id, Map<String, Object> dadosAtualizados) {
        Optional<Livro> livroOpt = lr.findById(id);

        if (livroOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(new RespostaModel("Livro com o ID " + id + " não encontrado."));
        }

        Livro livroExistente = livroOpt.get();

        dadosAtualizados.forEach((campo, valor) -> {
            switch (campo) {
                case "titulo":
                    if (valor != null) livroExistente.setTitulo(valor.toString());
                    break;
                case "autor":
                    if (valor != null) livroExistente.setAutor(valor.toString());
                    break;
                case "categoria":
                    if (valor != null) livroExistente.setCategoria(valor.toString());
                    break;
                case "editora":
                    if (valor != null) livroExistente.setEditora(valor.toString());
                    break;
                case "anoPublicacao":
                    if (valor != null) {
                        try {
                            Year ano = Year.parse(valor.toString());
                            livroExistente.setAnoPublicacao(ano);
                        } catch (DateTimeParseException e) {

                        }
                    }
                    break;
                case "descricao":
                    if (valor != null) livroExistente.setDescricao(valor.toString());
                    break;
                case "qtdTotal":
                    if (valor != null) {
                        int novaQtdTotal = Integer.parseInt(valor.toString());
                        int livrosEmprestados = livroExistente.getQtdTotal() - livroExistente.getQtdDisponivel();
                        int novaQtdDisponivel = novaQtdTotal - livrosEmprestados;

                        if (novaQtdDisponivel < 0) {
                            throw new RuntimeException("A nova quantidade total não pode ser menor que a quantidade de livros emprestados.");
                        }
                        livroExistente.setQtdTotal(novaQtdTotal);
                        livroExistente.setQtdDisponivel(novaQtdDisponivel);
                    }
                    break;
                case "statusLivro":
                    if (valor != null) {
                        try {
                            livroExistente.setStatusLivro(Livro.StatusLivro.valueOf(valor.toString()));
                        } catch (IllegalArgumentException e) {
                        }
                    }
                    break;
                case "capa":
                    if (valor instanceof MultipartFile) {
                        try {
                            byte[] imagemBytes = ((MultipartFile) valor).getBytes();
                            livroExistente.setCapa(imagemBytes);

                        } catch (IOException e) {
                            throw new RuntimeException("Erro ao processar o upload da capa.", e);
                        }
                    } else if (valor != null) {
                        try {
                            byte[] imagemBytes = java.util.Base64.getDecoder().decode(valor.toString());
                            livroExistente.setCapa(imagemBytes);
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Formato de imagem Base64 inválido.");
                        }
                    }
                    break;
        } });

        Livro livroSalvo = lr.save(livroExistente);
        LivroDTO dto = new LivroDTO(livroSalvo);

        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<RespostaModel> deletarLivro(Long id) {
        if (!lr.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new RespostaModel("Livro com o ID " + id + " não encontrado."));
        }

        // --- LÓGICA DE VERIFICAÇÃO CORRIGIDA ---
        if (reservaRepository.existsByLivroIdAndStatusReserva(id, Reserva.StatusReserva.ATIVA)) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new RespostaModel("Não é possível excluir o livro, pois ele possui reservas associadas."));
        }
        
        // Verifica se existem empréstimos NÃO FINALIZADOS associados a este livro
        if (emprestimoRepository.existsByLivroIdAndStatusEmprestimoNot(id, Emprestimo.StatusEmprestimo.FINALIZADO)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new RespostaModel("Não é possível excluir o livro, pois ele possui empréstimos associados."));
        }

        // ... (verificação de reservas) ...

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
    
    public DetalhesLivroResponseDTO buscarDetalhesPorId(Long id) {
        // 1. Busca o livro no banco de dados. Se não encontrar, lança um erro 404.
        Livro livro = lr.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro com o ID " + id + " não encontrado."));

        // 2. Tenta obter o usuário que está logado na sessão atual.
        Optional<Usuario> usuarioOpt = us.getUsuarioLogado();
        
        boolean usuarioJaReservou = false;
        // 3. Se houver um usuário logado...
        if (usuarioOpt.isPresent()) {
            Usuario usuarioLogado = usuarioOpt.get();
            // ...verifica no repositório de reservas se já existe uma reserva ATIVA para a combinação deste livro e deste usuário.
            usuarioJaReservou = reservaRepository.existsByLivroIdAndUsuarioIdAndStatusReserva(
                livro.getId(), 
                usuarioLogado.getId(), 
                Reserva.StatusReserva.ATIVA
            );
        }

        // 4. Cria e retorna o DTO de resposta, passando o livro e a flag calculada.
        return new DetalhesLivroResponseDTO(livro, usuarioJaReservou);
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

    // Método para buscar livro na barra de pesquisa
    public List<LivroDTO> buscarLivrosPorTermoGeral(String termo){
        List<Livro> livros = lr.buscarPorTermoGeral(termo);
        return livros.stream().map(LivroDTO::new).collect(Collectors.toList()); 
    }

    // Método de uso do findById
    public Optional<Livro> buscarPorId(Long id) {
        return lr.findById(id);
    }

}

