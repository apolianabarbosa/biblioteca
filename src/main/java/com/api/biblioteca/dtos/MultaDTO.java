package com.api.biblioteca.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.api.biblioteca.model.Multa.StatusMulta;

public record MultaDTO(
    Long id,
    BigDecimal valor,
    StatusMulta statusMulta,
    Long idEmprestimo,
    LocalDateTime dataEmprestimo,
    String tituloLivro,
    String nomeUsuario
) {}