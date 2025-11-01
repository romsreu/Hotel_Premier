package ar.utn.hotel.dto;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "habitacion")
public class HabitacionDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHabitacion;

    private String numero;
    private String tipo;
    private boolean ocupada;

    @ManyToOne
    @JoinColumn(name = "id_reserva")
    private ReservaDTO reserva;

}
