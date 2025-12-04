package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.PersonaDAOImpl;
import ar.utn.hotel.dao.impl.ReservaDAOImpl;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.model.Persona;
import ar.utn.hotel.model.Reserva;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class GestorReserva {

    private final ReservaDAO reservaDAO;
    private final PersonaDAO personaDAO;

    public GestorReserva(ReservaDAO reservaDAO, PersonaDAO personaDAO) {
        this.reservaDAO = reservaDAO;
        this.personaDAO = personaDAO;
    }

    public GestorReserva() {
        PersonaDAOImpl personaDAO = new PersonaDAOImpl();
        EstadoHabitacionDAOImpl estadoDAO = new EstadoHabitacionDAOImpl();
        this.reservaDAO = new ReservaDAOImpl(personaDAO, estadoDAO);
        this.personaDAO = personaDAO;
    }

    /**
     * Crea una o múltiples reservas según las habitaciones y fechas proporcionadas.
     * Retorna una lista de ReservaDTO (una por cada habitación reservada).
     *
     * Si el DTO contiene una sola habitación, la lista tendrá un solo elemento.
     * Si contiene múltiples habitaciones con las mismas fechas, crea una reserva por cada una.
     */
    public List<ReservaDTO> crearReserva(ReservaDTO dto) throws Exception {
        validarReservaDTO(dto);

        Persona p = personaDAO.buscarPorNombreApellido(dto.getNombrePersona(), dto.getApellidoPersona());

        if (p == null) {
            throw new IllegalArgumentException("Error: La persona no existe en el sistema.");
        }

        // Convertir a Map<Integer, RangoFechas>
        // Como el DTO usa las MISMAS fechas para todas las habitaciones,
        // creamos un RangoFechas idéntico para cada habitación
        Map<Integer, ReservaDAO.RangoFechas> habitacionesConFechas = new HashMap<>();

        for (Integer numeroHab : dto.getNumerosHabitaciones()) {
            habitacionesConFechas.put(
                    numeroHab,
                    new ReservaDAO.RangoFechas(dto.getFechaInicio(), dto.getFechaFin())
            );
        }

        // Crear las reservas (una por habitación)
        List<Reserva> reservas = reservaDAO.crearReservas(
                p.getId(),
                habitacionesConFechas
        );

        // Convertir todas las reservas a DTOs
        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método de conveniencia que crea una reserva con fechas específicas por habitación.
     * Útil cuando cada habitación puede tener fechas diferentes.
     *
     * @param nombrePersona Nombre de la persona
     * @param apellidoPersona Apellido de la persona
     * @param habitacionesConFechas Map con número de habitación y sus fechas específicas
     * @return Lista de reservas creadas
     */
    public List<ReservaDTO> crearReservasConFechasEspecificas(
            String nombrePersona,
            String apellidoPersona,
            Map<Integer, ReservaDAO.RangoFechas> habitacionesConFechas) throws Exception {

        if (nombrePersona == null || nombrePersona.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la persona es obligatorio");
        }

        if (apellidoPersona == null || apellidoPersona.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido de la persona es obligatorio");
        }

        if (habitacionesConFechas == null || habitacionesConFechas.isEmpty()) {
            throw new IllegalArgumentException("Debe especificar al menos una habitación con fechas");
        }

        Persona p = personaDAO.buscarPorNombreApellido(nombrePersona, apellidoPersona);

        if (p == null) {
            throw new IllegalArgumentException("Error: La persona no existe en el sistema.");
        }

        // Crear las reservas
        List<Reserva> reservas = reservaDAO.crearReservas(p.getId(), habitacionesConFechas);

        // Convertir a DTOs
        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

    /**
     * Convierte una entidad Reserva a DTO.
     * Nota: Ahora una Reserva tiene UNA habitación (no un Set)
     */
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
                        // Una reserva tiene UNA habitación
                        Collections.singleton(reserva.getHabitacion().getNumero())
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

    public Reserva buscarReservaPorHabitacionYFecha(Integer numeroHabitacion, LocalDate fecha) {
        try {
            List<Reserva> reservas = reservaDAO.obtenerTodas();

            return reservas.stream()
                    .filter(r -> r.getHabitacion() != null &&
                            r.getHabitacion().getNumero().equals(numeroHabitacion))
                    .filter(r -> !fecha.isBefore(r.getFechaInicio()) &&
                            !fecha.isAfter(r.getFechaFin()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error al buscar reserva: " + e.getMessage());
            return null;
        }
    }
}