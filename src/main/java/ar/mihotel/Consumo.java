package ar.mihotel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Consumo {
    private int id_consumo;
    private String nombre;
    private double costo;
}