package ar.mihotel;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Huesped {
    private int id_persona;
    private int  nro_documento;
    private String tipo_documento;
    private String posicion_iva;
    private Date fecha_nacimiento;
    private String ocupacion;
    private String nacionalidad;
    private String email;
    private int cuit;

}
