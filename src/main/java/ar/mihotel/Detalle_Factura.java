package ar.mihotel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Detalle_Factura {
    private int id_detalle;
    private String concepto;
    private int cantidad;
    private double precio_unitario;
    private double precio_total;

}
