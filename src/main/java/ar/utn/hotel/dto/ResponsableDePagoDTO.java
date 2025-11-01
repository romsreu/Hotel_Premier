package ar.utn.hotel.dto;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "responsable_pago")
public class ResponsableDePagoDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResponsable;

    private String nombre;
    private String apellido;
    private String medioPago;

    @OneToMany(mappedBy = "responsableDePago", cascade = CascadeType.ALL)
    private List<ReservaDTO> reservas;
}
