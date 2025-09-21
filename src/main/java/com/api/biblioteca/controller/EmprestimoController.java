package com.api.biblioteca.controller;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.dtos.EmprestimoDTO;
import com.api.biblioteca.dtos.EmprestimoRequestDTO;
import com.api.biblioteca.model.Emprestimo;
import com.api.biblioteca.service.EmprestimoService;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    @Autowired
    private EmprestimoService emprestimoService;

    // Endpoint para criar um novo empréstimo
    @PostMapping("/registrar")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<EmprestimoDTO> criarEmprestimo(@RequestBody EmprestimoRequestDTO requestDTO) {
        Emprestimo novoEmprestimo = emprestimoService.criarEmprestimo(requestDTO.getIdUsuario(), requestDTO.getIdLivro());
        EmprestimoDTO dto = emprestimoService.toDTO(novoEmprestimo);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // Endpoint para registrar a devolução de um livro
    @PutMapping("/devolver/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<EmprestimoDTO> devolverLivro(@PathVariable Long id) { 
        Emprestimo emprestimoDevolvido = emprestimoService.devolverLivro(id);
        EmprestimoDTO dto = emprestimoService.toDTO(emprestimoDevolvido);
        return ResponseEntity.ok(dto);
    }

    // Endpoint para listar todos os empréstimos
    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<List<EmprestimoDTO>> listarTodos() { 
        List<Emprestimo> emprestimos = emprestimoService.listarTodos();
        List<EmprestimoDTO> dtos = emprestimos.stream()
                .map(emprestimoService::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    // Endpoint para buscar um empréstimo pelo seu ID
    @GetMapping("/buscar/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'LEITOR')")
    public ResponseEntity<EmprestimoDTO> buscarPorId(@PathVariable Long id) { 
        return emprestimoService.buscarPorId(id)
                .map(emprestimoService::toDTO) 
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para listar todos os empréstimos de um usuário específico
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyRole('LEITOR', 'BIBLIOTECARIO')")
    public ResponseEntity<List<EmprestimoDTO>> listarPorUsuario(@PathVariable Long idUsuario) {
        List<Emprestimo> emprestimos = emprestimoService.listarPorUsuario(idUsuario);

        List<EmprestimoDTO> emprestimosDTO = emprestimos.stream()
                .map(emprestimoService::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(emprestimosDTO);
    }
}
