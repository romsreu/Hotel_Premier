package ar.utn.hotel.model;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Pago {
    private int id_pago;
    private String moneda;
    private int numero;
    private Date fecha;
    private double monto;
    private double cotizacion;
}
