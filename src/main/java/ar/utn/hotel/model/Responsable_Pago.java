package ar.utn.hotel.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Responsable_Pago {
    private int idResponsablePago;
    private String razonSocial;
    private int cuit;
    private int telefono;
    private boolean personaJuridica;
}