package ar.mihotel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Persona {
    private int id_Persona;
    private String nombre;
    private String apellido;
    private int telefono;
}
