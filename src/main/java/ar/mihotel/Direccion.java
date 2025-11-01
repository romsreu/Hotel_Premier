package ar.mihotel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Direccion {
    private int idDireccion;
    private String calle;
    private int numero;
    private String departamento;
    private int piso;
    private int codPostal;
    private String localidad;
    private String pais;
    private String provincia;
}