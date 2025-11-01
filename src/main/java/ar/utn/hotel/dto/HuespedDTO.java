package ar.utn.hotel.dto;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "huesped")
public class HuespedDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHuesped;

    private String nombre;
    private String apellido;
    private String tipoDocumento;
    private String nroDocumento;
    private LocalDate fechaNacimiento;

    @ManyToMany(mappedBy = "huespedes")
    private List<ReservaDTO> reservas;
}
