package ar.utn.hotel.model;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Estadia {
    private int id_estadia;
    private Date fecha_inicio;
    private Date fecha_fin;
    private Date hora_check_in;
    private Date hora_check_out;
}
