package ar.utn.hotel.model;

import enums.EstadoHabitacion;
import enums.TipoHabitacion;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "habitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {
    @Id
    @Column(name = "numero", nullable = false, unique = true)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoHabitacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoHabitacion estado;

    @Column(name = "costo_noche", nullable = false)
    private Double costoNoche;

    @Column(name = "piso")
    private Integer piso;

    @ManyToMany(mappedBy = "habitaciones")
    @Builder.Default
    private Set<Reserva> reservas = new HashSet<>();

    @Override
    public String toString() {
        return "Habitacion{" +
                "numero=" + numero +
                ", tipo='" + tipo + '\'' +
                ", costoNoche=" + costoNoche +
                '}';
    }
}