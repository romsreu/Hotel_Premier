package ar.utn.hotel.model;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Factura {
    private int id_factura;
    private int numero;
    private Date fecha_emision;
    private String tipo;
    private String estado;
    private double precio_total;
}

