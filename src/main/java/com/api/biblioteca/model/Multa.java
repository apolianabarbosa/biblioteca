package com.api.biblioteca.model;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "multas")
@Getter
@Setter
public class Multa {
    
    public enum StatusMulta{
        PENDENTE, PAGO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_multa")
    private Long id;

    @Column(precision = 10, scale = 0)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_multa")
    private StatusMulta statusMulta;

    @OneToOne
    @JoinColumn(name = "fk_id_emprestimo", nullable = false, unique = true)
    private Emprestimo emprestimo;
}
