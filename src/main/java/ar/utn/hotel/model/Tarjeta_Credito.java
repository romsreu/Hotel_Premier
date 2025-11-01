package ar.utn.hotel.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Tarjeta_Credito {
    private int id_tarjeta;
    private String Marca;
    private String banco_emisor;

}
