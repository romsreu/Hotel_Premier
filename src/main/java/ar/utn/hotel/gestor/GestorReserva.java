package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.Persona;
import ar.utn.hotel.model.Reserva;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class GestorReserva {

    private final ReservaDAO reservaDAO;
    private final PersonaDAO personaDAO;

    public GestorReserva(ReservaDAO reservaDAO, PersonaDAO personaDAO) {
        this.reservaDAO = reservaDAO;
        this.personaDAO = personaDAO;
    }

    public ReservaDTO crearReserva(ReservaDTO dto) throws Exception {
        validarReservaDTO(dto);

        Persona p = personaDAO.buscarPorNombreApellido(dto.getNombrePersona(), dto.getApellidoPersona());

        if (p == null) {
            throw new IllegalArgumentException("Error: La persona no existe en el sistema.");
        }

        Reserva reserva = reservaDAO.crearReserva(
                p.getId(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                dto.getNumerosHabitaciones()
        );

        return toDTO(reserva);
    }

    public ReservaDTO obtenerReserva(Long id) {
        Reserva reserva = reservaDAO.obtenerPorId(id);

        if (reserva == null) {
            throw new IllegalArgumentException("No existe reserva con el ID " + id);
        }

        return toDTO(reserva);
    }

    public List<ReservaDTO> listarReservas() {
        List<Reserva> reservas = reservaDAO.obtenerTodas();

        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> obtenerReservasPorPersona(Long idPersona) {
        List<Reserva> reservas = reservaDAO.obtenerPorPersona(idPersona);

        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> obtenerReservasPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        List<Reserva> reservas = reservaDAO.obtenerPorFechas(fechaInicio, fechaFin);

        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void cancelarReserva(Long id) {
        Reserva reserva = reservaDAO.obtenerPorId(id);
        if (reserva == null) {
            throw new IllegalArgumentException("No existe reserva con el ID " + id);
        }

        reservaDAO.eliminar(id);
    }

    private ReservaDTO toDTO(Reserva reserva) {
        return ReservaDTO.builder()
                .id(reserva.getId())
                .idPersona(reserva.getPersona().getId())
                .nombrePersona(reserva.getPersona().getNombre())
                .apellidoPersona(reserva.getPersona().getApellido())
                .telefonoPersona(reserva.getPersona().getTelefono())
                .fechaInicio(reserva.getFechaInicio())
                .fechaFin(reserva.getFechaFin())
                .numerosHabitaciones(
                        reserva.getHabitaciones().stream()
                                .map(Habitacion::getNumero)
                                .collect(Collectors.toSet())
                )
                .build();
    }

    private void validarReservaDTO(ReservaDTO dto) {
        if (dto.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }

        if (dto.getFechaFin() == null) {
            throw new IllegalArgumentException("La fecha de fin es obligatoria");
        }

        if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin"
            );
        }

        if (dto.getNumerosHabitaciones() == null || dto.getNumerosHabitaciones().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos una habitación"
            );
        }
    }
}