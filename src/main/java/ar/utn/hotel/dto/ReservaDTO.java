package ar.utn.hotel.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {

    private Long id;
    private Long idPersona;
    private String nombrePersona;
    private String apellidoPersona;
    private String telefonoPersona;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Set<Integer> numerosHabitaciones;

    @Override
    public String toString() {
        return "ReservaDTO{" +
                "id=" + id +
                ", idPersona=" + idPersona +
                ", nombrePersona='" + nombrePersona + '\'' +
                ", apellidoPersona='" + apellidoPersona + '\'' +
                ", telefonoPersona='" + telefonoPersona + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", numerosHabitaciones=" + numerosHabitaciones +
                '}';
    }
}