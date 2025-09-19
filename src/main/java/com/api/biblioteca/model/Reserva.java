package com.api.biblioteca.model;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reservas")
@Getter
@Setter
public class Reserva {
    
    public enum StatusReserva {
        ATIVA, CANCELADA, ATENDIDA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;
    
    @Column(name = "data_reserva")
    private LocalDateTime dataReserva;

    @PrePersist
    public void PrePersist(){
        this.dataReserva = LocalDateTime.now();
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_reserva")
    private StatusReserva statusReserva;

    @ManyToOne
    @JoinColumn(name = "fk_id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "fk_id_livro", nullable = false)
    private Livro livro;
}
