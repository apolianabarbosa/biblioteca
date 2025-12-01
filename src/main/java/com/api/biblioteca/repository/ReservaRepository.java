package com.api.biblioteca.repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.biblioteca.dtos.RelatorioItemDTO;
import com.api.biblioteca.model.Livro;
import com.api.biblioteca.model.Reserva;
import com.api.biblioteca.model.Reserva.StatusReserva;
import com.api.biblioteca.model.Usuario;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
     // MÉTODOS BASES
    // Listagem Global Ordenada do sistema mais recente ao mais antigo
    List<Reserva> findAllByOrderByDataReservaAsc();

    // MÉTODOS - BIBLIOTECARIO
    // Busca dinamica (reserva de um livro + status da reserva)
    List<Reserva> findFirstByLivroAndStatusReservaOrderByDataReservaAsc(Livro livro, StatusReserva statusReserva);

    // Listagem - retorna detalhes do livro com a iformação de n pessoas na espera se status for ativa
    long  countByLivroAndStatusReserva(Livro livro, StatusReserva statusReserva);

    Optional<Reserva> findTopByLivroAndStatusReservaOrderByDataReserva(Livro livro, StatusReserva statusReserva);
    
    // MÉTODOS - LEITOR
    // Listagem de todas as reserva de um usuário comum
    List<Reserva> findByUsuario(Usuario usuario);

    // Busca por reservas para o usuario + status da reserva
    List<Reserva> findByUsuarioAndStatusReserva(Usuario usuario, StatusReserva statusReserva);

    List<Reserva> findByUsuarioAndLivroAndStatusReservaOrderByDataReservaAsc(Usuario usuario, Livro livro, StatusReserva status);

    // Metodo para verificar a existencia de reservas em biblioteca service
    boolean existsByLivroId(Long livroId);

    long countByLivroId(Long livroId);

    long countByStatusReserva(Reserva.StatusReserva status);

    // Metodo para verificar se o usuário já tem reserva
    boolean existsByUsuarioAndLivroAndStatusReserva(Usuario usuario, Livro livro, StatusReserva statusReserva);

    boolean existsByLivroAndStatusReserva(Livro livro, StatusReserva statusReserva);
    
    boolean existsByLivroIdAndStatusReserva(Long livroId, StatusReserva statusReserva); 

    boolean existsByLivroIdAndUsuarioIdAndStatusReserva(Long idLivro, Long idUsuario, StatusReserva statusReserva);

    boolean existsByLivroAndStatusReservaAndDataReservaBefore(Livro livro, StatusReserva statusReserva, LocalDateTime data);

    // Top 5 sem filtro
    @Query("SELECT NEW com.api.biblioteca.dtos.RelatorioItemDTO(r.livro.titulo, COUNT(r)) " +
           "FROM Reserva r GROUP BY r.livro.titulo ORDER BY COUNT(r) DESC")
    List<RelatorioItemDTO> findLivrosMaisReservados(Pageable pageable);

    // Top 5 com filtro por data e retorno do Título
    @Query("SELECT NEW com.api.biblioteca.dtos.RelatorioItemDTO(r.livro.titulo, COUNT(r)) " +
           "FROM Reserva r " +
           "WHERE (:startDate IS NULL OR r.dataReserva >= :startDate) " +
           "AND (:endDate IS NULL OR r.dataReserva <= :endDate) " +
           "GROUP BY r.livro.titulo " +
           "ORDER BY COUNT(r) DESC")
    List<RelatorioItemDTO> findLivrosMaisReservadosComFiltro(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable);

}
