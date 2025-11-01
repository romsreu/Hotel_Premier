package ar.mihotel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Tipo_Habitacion {
    private int idTipoHabitacion;
    private String nombre;
    private String descripcion;
    private int capacidad;
    private int costoNoche;
}