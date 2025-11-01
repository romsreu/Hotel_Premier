package ar.utn.hotel.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Habitacion {
    private int id_habitacion;
    private int numero;
    private int piso;
    private int idTipoHabitacion;
}
