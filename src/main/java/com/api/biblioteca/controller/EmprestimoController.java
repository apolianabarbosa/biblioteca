package com.api.biblioteca.controller;


import com.api.biblioteca.dtos.EmprestimoRequestDTO;
import com.api.biblioteca.model.Emprestimo;
import com.api.biblioteca.service.EmprestimoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    @Autowired
    private EmprestimoService emprestimoService;

    // Endpoint para criar um novo empréstimo
    @PostMapping
    public ResponseEntity<Emprestimo> criarEmprestimo(@RequestBody EmprestimoRequestDTO requestDTO) {
        Emprestimo novoEmprestimo = emprestimoService.criarEmprestimo(requestDTO.getIdUsuario(), requestDTO.getIdLivro());
        return new ResponseEntity<>(novoEmprestimo, HttpStatus.CREATED);
    }

    // Endpoint para registrar a devolução de um livro
    @PutMapping("/{id}/devolver")
    public ResponseEntity<Emprestimo> devolverLivro(@PathVariable Long id) {
        Emprestimo emprestimoDevolvido = emprestimoService.devolverLivro(id);
        return ResponseEntity.ok(emprestimoDevolvido);
    }

    // Endpoint para listar todos os empréstimos
    @GetMapping
    public ResponseEntity<List<Emprestimo>> listarTodos() {
        List<Emprestimo> emprestimos = emprestimoService.listarTodos();
        return ResponseEntity.ok(emprestimos);
    }

    // Endpoint para buscar um empréstimo pelo seu ID
    @GetMapping("/{id}")
    public ResponseEntity<Emprestimo> buscarPorId(@PathVariable Long id) {
        return emprestimoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para listar todos os empréstimos de um usuário específico
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Emprestimo>> listarPorUsuario(@PathVariable Long idUsuario) {
        List<Emprestimo> emprestimos = emprestimoService.listarPorUsuario(idUsuario);
        return ResponseEntity.ok(emprestimos);
    }
}