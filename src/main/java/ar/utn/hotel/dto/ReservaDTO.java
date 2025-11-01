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
@Table(name = "reserva")
public class ReservaDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReserva;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @ManyToOne
    @JoinColumn(name = "id_responsable")
    private ResponsableDePagoDTO responsableDePago;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<HabitacionDTO> habitaciones;

    @ManyToMany
    @JoinTable(
            name = "huesped_reserva",
            joinColumns = @JoinColumn(name = "id_reserva"),
            inverseJoinColumns = @JoinColumn(name = "id_huesped")
    )
    private List<HuespedDTO> huespedes;

}
