package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dto.HabitacionDTO;
import ar.utn.hotel.model.Habitacion;
import enums.TipoHabitacion;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GestorHabitacion {

    private final HabitacionDAO habitacionDAO;

    public GestorHabitacion(HabitacionDAO habitacionDAO) {
        this.habitacionDAO = habitacionDAO;
    }

    public void crearHabitacion(HabitacionDTO dto) {
        validarHabitacionDTO(dto);

        // Verificar que no exista ya
        Habitacion existente = habitacionDAO.buscarPorNumero(dto.getNumero());
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe una habitación con el número " + dto.getNumero());
        }

        Habitacion habitacion = Habitacion.builder()
                .numero(dto.getNumero())
                .tipo(TipoHabitacion.valueOf(dto.getTipo()))
                .costoNoche(dto.getCostoNoche())
                .build();

        habitacionDAO.guardar(habitacion);
    }

    public List<HabitacionDTO> obtenerHabitacionesDisponibles(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin"
            );
        }

        List<Habitacion> habitaciones = habitacionDAO.listarPorRangoDeFechas(fechaInicio, fechaFin);

        return habitaciones.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Reserva una o más habitaciones (cambia su estado a RESERVADA)
    public void reservarHabitaciones(Set<Integer> numerosHabitaciones) {
        if (numerosHabitaciones == null || numerosHabitaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe proporcionar al menos una habitación para reservar"
            );
        }

        habitacionDAO.reservarHabitaciones(numerosHabitaciones);
    }

    // Obtiene una habitación por su número y la convierte a DTO
    public HabitacionDTO obtenerHabitacion(Integer numero) {
        Habitacion habitacion = habitacionDAO.buscarPorNumero(numero);

        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + numero);
        }

        return toDTO(habitacion);
    }

    public List<HabitacionDTO> obtenerTodasHabitaciones() {
        List<Habitacion> habitaciones = habitacionDAO.listarTodas();

        return habitaciones.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void actualizarHabitacion(HabitacionDTO dto) {
        validarHabitacionDTO(dto);

        Habitacion habitacion = habitacionDAO.buscarPorNumero(dto.getNumero());
        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + dto.getNumero());
        }

        habitacion.setTipo(TipoHabitacion.valueOf(dto.getTipo()));
        habitacion.setCostoNoche(dto.getCostoNoche());

        habitacionDAO.actualizar(habitacion);
    }

    public void eliminarHabitacion(Integer numero) {
        Habitacion habitacion = habitacionDAO.buscarPorNumero(numero);
        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + numero);
        }

        habitacionDAO.eliminar(numero);
    }

    private HabitacionDTO toDTO(Habitacion habitacion) {
        return HabitacionDTO.builder()
                .numero(habitacion.getNumero())
                .tipo(String.valueOf(habitacion.getTipo()))
                .costoNoche(habitacion.getCostoNoche())
                .build();
    }

    // Valida los datos del DTO
    private void validarHabitacionDTO(HabitacionDTO dto) {
        if (dto.getNumero() == null || dto.getNumero() <= 0) {
            throw new IllegalArgumentException("El número de habitación es inválido");
        }

        if (dto.getTipo() == null || dto.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de habitación es obligatorio");
        }

        if (dto.getCostoNoche() == null || dto.getCostoNoche() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
    }
}