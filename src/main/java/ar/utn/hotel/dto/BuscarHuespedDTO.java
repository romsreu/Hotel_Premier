package ar.utn.hotel.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuscarHuespedDTO {

    private String nombre;
    private String apellido;
    private String numeroDocumento;
    private String tipoDocumento;
}
