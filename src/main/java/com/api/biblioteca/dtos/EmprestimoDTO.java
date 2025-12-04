package com.api.biblioteca.dtos;
import java.time.LocalDateTime;

import com.api.biblioteca.model.Emprestimo;

public record EmprestimoDTO(
    Long id,
    LocalDateTime dataEmprestimo,
    LocalDateTime dataPrevistaDevolucao,
    LocalDateTime dataDevolucaoReal,
    Emprestimo.StatusEmprestimo statusEmprestimo,
    LivroResumidoDTO livro,
    UsuarioResumidoDTO usuario
) {}