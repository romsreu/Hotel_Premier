package ar.utn.hotel.dao;

import ar.utn.hotel.model.Reserva;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ReservaDAO {
    Reserva crearReserva(Long idPersona, LocalDate fechaInicio, LocalDate fechaFin, Set<Integer> numerosHabitaciones);

    Reserva crearReservaPorNombreApellido(String nombre, String apellido, LocalDate fechaInicio, LocalDate fechaFin, Set<Integer> numerosHabitaciones);

    Reserva crearReservaPorTelefono(String telefono, LocalDate fechaInicio, LocalDate fechaFin, Set<Integer> numerosHabitaciones);

    Reserva obtenerPorId(Long id);

    List<Reserva> obtenerTodas();

    List<Reserva> obtenerPorPersona(Long idPersona);

    List<Reserva> obtenerPorFechas(LocalDate fechaInicio, LocalDate fechaFin);

    List<Reserva> obtenerPorHabitacion(Integer numeroHabitacion);

    void actualizar(Reserva reserva);

    void eliminar(Long id);
}