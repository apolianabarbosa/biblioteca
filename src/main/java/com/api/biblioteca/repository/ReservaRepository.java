package com.api.biblioteca.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.Reserva;
import com.api.biblioteca.model.Reserva.StatusReserva;
import com.api.biblioteca.model.Usuario;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
     // MÉTODOS BASES
    // Listagem Global Ordenada do sistema mais recente ao mais antigo
    List<Reserva> findAllByOrderByDataReservaDesc();

    // MÉTODOS - BIBLIOTECARIO
    // Busca dinamica (reserva de um livro + status da reserva)
    List<Reserva> findFirstByLivroAndStatusReservaOrderByDataReservaAsc(Livro livro, StatusReserva statusReserva);

    // Listagem - retorna detalhes do livro com a iformação de n pessoas na espera se status for ativa
    long  countByLivroAndStatusReserva(Livro livro, StatusReserva statusReserva);
    
    // MÉTODOS - LEITOR
    // Listagem de todas as reserva de um usuário comum
    List<Reserva> findByUsuario(Usuario usuario);

    // Busca por reservas para o usuario + status da reserva
    List<Reserva> findByUsuarioAndStatusReserva(Usuario usuario, StatusReserva statusReserva);

    List<Reserva> findByUsuarioAndLivroAndStatusReservaOrderByDataReservaAsc(Usuario usuario, Livro livro, StatusReserva status);

    // Metodo para verificar a existencia de reservas em biblioteca service
    boolean existsByLivroId(Long livroId);

    // Metodo para verificar se o usuário já tem reserva
    boolean existsByUsuarioAndLivroAndStatusReserva(Usuario usuario, Livro livro, StatusReserva statusReserva);

    boolean existsByLivroAndStatusReserva(Livro livro, StatusReserva status);
    
    boolean existsByLivroIdAndStatusReserva(Long livroId, Reserva.StatusReserva status); 

    boolean existsByLivroIdAndUsuarioIdAndStatusReserva(Long idLivro, Long idUsuario, StatusReserva status);
}
