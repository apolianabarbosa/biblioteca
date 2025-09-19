package com.api.biblioteca.dtos;

import com.api.biblioteca.model.Reserva.StatusReserva;
import java.time.LocalDateTime;

public record ReservaDTO(
    Long id,
    LocalDateTime dataReserva,
    StatusReserva statusReserva,
    LivroResumidoDTO livro,
    UsuarioResumidoDTO usuario
    
) {}
    