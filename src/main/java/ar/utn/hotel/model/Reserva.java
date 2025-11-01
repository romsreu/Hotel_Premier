package ar.utn.hotel.model;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Reserva {
    private int id;
    private Date fecha_inicio;
    private Date fecha_fin;
    private int cant_huespedes;
}
