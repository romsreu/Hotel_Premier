package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.PersonaDAOImpl;
import ar.utn.hotel.dao.impl.ReservaDAOImpl;
import ar.utn.hotel.dao.impl.TipoEstadoDAOImpl;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.model.Persona;
import ar.utn.hotel.model.Reserva;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestor que maneja la lógica de negocio relacionada con las reservas.
 * Coordina con GestorHabitacion para cambiar estados de habitaciones.
 */
public class GestorReserva {

    private final ReservaDAO reservaDAO;
    private final PersonaDAO personaDAO;
    private GestorHabitacion gestorHabitacion; // Referencia circular controlada

    public GestorReserva(ReservaDAO reservaDAO, PersonaDAO personaDAO) {
        this.reservaDAO = reservaDAO;
        this.personaDAO = personaDAO;
    }

    public GestorReserva() {
        this.personaDAO = new PersonaDAOImpl();
        TipoEstadoDAOImpl tipoEstadoDAO = new TipoEstadoDAOImpl();
        this.reservaDAO = new ReservaDAOImpl(personaDAO, tipoEstadoDAO);
    }

    /**
     * Establece la referencia al gestor de habitaciones (para evitar dependencia circular en constructor)
     */
    public void setGestorHabitacion(GestorHabitacion gestorHabitacion) {
        this.gestorHabitacion = gestorHabitacion;
    }

    /**
     * Crea una o múltiples reservas según las habitaciones y fechas proporcionadas.
     * IMPORTANTE: Este método también cambia el estado de las habitaciones a RESERVADA.
     *
     * @param dto DTO con la información de la reserva
     * @return Lista de ReservaDTO (una por cada habitación reservada)
     * @throws Exception si hay algún error en el proceso
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

        // Cambiar estado de habitaciones a RESERVADA
        if (gestorHabitacion != null) {
            gestorHabitacion.reservarHabitaciones(
                    dto.getNumerosHabitaciones(),
                    dto.getFechaInicio(),
                    dto.getFechaFin()
            );
        }

        // Convertir todas las reservas a DTOs
        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método de conveniencia que crea una reserva con fechas específicas por habitación.
     * Útil cuando cada habitación puede tener fechas diferentes.
     * IMPORTANTE: También cambia el estado de las habitaciones a RESERVADA.
     *
     * @param nombrePersona Nombre de la persona
     * @param apellidoPersona Apellido de la persona
     * @param habitacionesConFechas Map con número de habitación y sus fechas específicas
     * @return Lista de reservas creadas
     * @throws Exception si hay algún error en el proceso
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

        // Cambiar estado de cada habitación a RESERVADA con sus fechas específicas
        if (gestorHabitacion != null) {
            for (Map.Entry<Integer, ReservaDAO.RangoFechas> entry : habitacionesConFechas.entrySet()) {
                Set<Integer> habitacion = Collections.singleton(entry.getKey());
                ReservaDAO.RangoFechas rango = entry.getValue();

                gestorHabitacion.reservarHabitaciones(
                        habitacion,
                        rango.getFechaInicio(),
                        rango.getFechaFin()
                );
            }
        }

        // Convertir a DTOs
        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una reserva por su ID
     */
    public ReservaDTO obtenerReserva(Long id) {
        Reserva reserva = reservaDAO.obtenerPorId(id);

        if (reserva == null) {
            throw new IllegalArgumentException("No existe reserva con el ID " + id);
        }

        return toDTO(reserva);
    }

    /**
     * Obtiene la entidad Reserva (no DTO) por ID
     * Útil para otros gestores que necesitan la entidad completa
     */
    public Reserva obtenerReservaEntidad(Long id) {
        return reservaDAO.obtenerPorId(id);
    }

    /**
     * Lista todas las reservas
     */
    public List<ReservaDTO> listarReservas() {
        List<Reserva> reservas = reservaDAO.obtenerTodas();

        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas por persona
     */
    public List<ReservaDTO> obtenerReservasPorPersona(Long idPersona) {
        List<Reserva> reservas = reservaDAO.obtenerPorPersona(idPersona);

        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas en un rango de fechas
     */
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

    /**
     * Cancela una reserva y libera la habitación (cambia estado a DISPONIBLE)
     */
    public void cancelarReserva(Long id) {
        Reserva reserva = reservaDAO.obtenerPorId(id);
        if (reserva == null) {
            throw new IllegalArgumentException("No existe reserva con el ID " + id);
        }

        // Verificar que la reserva no tenga estadía asociada
        if (reserva.getEstadia() != null) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva que ya tiene una estadía asociada (check-in realizado)"
            );
        }

        // Liberar la habitación (cambiar estado a DISPONIBLE)
        if (gestorHabitacion != null) {
            Set<Integer> habitacion = Collections.singleton(reserva.getHabitacion().getNumero());
            gestorHabitacion.liberarHabitaciones(habitacion);
        }

        // Eliminar la reserva
        reservaDAO.eliminar(id);
    }

    /**
     * Busca una reserva por habitación y fecha
     * Útil para verificar si existe una reserva antes de crear una estadía
     */
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

    /**
     * Busca reservas activas (sin estadía) por habitación
     */
    public List<ReservaDTO> buscarReservasActivasPorHabitacion(Integer numeroHabitacion) {
        List<Reserva> todasReservas = reservaDAO.obtenerTodas();

        return todasReservas.stream()
                .filter(r -> r.getHabitacion() != null &&
                        r.getHabitacion().getNumero().equals(numeroHabitacion))
                .filter(r -> r.getEstadia() == null) // Sin estadía = reserva activa
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Reserva a DTO.
     * Nota: Una Reserva tiene UNA habitación (no un Set)
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
                .cantHuespedes(reserva.getCantHuespedes())
                .descuento(reserva.getDescuento())
                .numerosHabitaciones(
                        // Una reserva tiene UNA habitación
                        Collections.singleton(reserva.getHabitacion().getNumero())
                )
                .tieneEstadia(reserva.getEstadia() != null)
                .build();
    }

    /**
     * Valida los datos del DTO de reserva
     */
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

        if (dto.getFechaInicio().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser anterior a hoy"
            );
        }

        if (dto.getNumerosHabitaciones() == null || dto.getNumerosHabitaciones().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos una habitación"
            );
        }

        if (dto.getNombrePersona() == null || dto.getNombrePersona().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la persona es obligatorio");
        }

        if (dto.getApellidoPersona() == null || dto.getApellidoPersona().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido de la persona es obligatorio");
        }
    }
}