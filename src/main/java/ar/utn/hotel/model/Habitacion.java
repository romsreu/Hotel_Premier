package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "habitacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {

    @Id
    @Column(nullable = false, unique = true)
    private Integer numero;

    @ManyToOne
    @JoinColumn(name = "id_tipo_habitacion", nullable = false)
    private TipoHabitacion tipo;

    @Column(nullable = false)
    private Integer piso;

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RegistroEstadoHabitacion> historialEstados = new HashSet<>();

    @OneToMany(mappedBy = "habitacion")
    @Builder.Default
    private Set<Reserva> reservas = new HashSet<>();

    // Métodos helper
    public Double getCostoNoche() {
        return tipo != null ? tipo.getCostoNoche() : 0.0;
    }

    public RegistroEstadoHabitacion getEstadoActual() {
        return historialEstados.stream()
                .filter(r -> r.getFechaHasta() == null ||
                        !r.getFechaHasta().isBefore(LocalDate.now()))
                .findFirst()
                .orElse(null);
    }

    public RegistroEstadoHabitacion getEstadoEn(LocalDate fecha) {
        return historialEstados.stream()
                .filter(r -> !r.getFechaDesde().isAfter(fecha) &&
                        (r.getFechaHasta() == null || !r.getFechaHasta().isBefore(fecha)))
                .findFirst()
                .orElse(null);
    }
}