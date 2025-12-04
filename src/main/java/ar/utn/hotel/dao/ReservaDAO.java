package ar.utn.hotel.dao;

import ar.utn.hotel.model.Reserva;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReservaDAO {

    // Crear múltiples reservas (una por cada habitación con sus fechas)
    List<Reserva> crearReservas(Long idPersona, Map<Integer, RangoFechas> habitacionesConFechas);

    List<Reserva> crearReservasPorNombreApellido(String nombre, String apellido,
                                                 Map<Integer, RangoFechas> habitacionesConFechas);

    List<Reserva> crearReservasPorTelefono(String telefono,
                                           Map<Integer, RangoFechas> habitacionesConFechas);

    // CRUD básico
    Reserva obtenerPorId(Long id);
    List<Reserva> obtenerTodas();
    List<Reserva> obtenerPorPersona(Long idPersona);
    List<Reserva> obtenerPorFechas(LocalDate fechaInicio, LocalDate fechaFin);
    List<Reserva> obtenerPorHabitacion(Integer numeroHabitacion);
    void actualizar(Reserva reserva);
    void eliminar(Long id);

    // Clase auxiliar para transportar rangos de fechas
    @lombok.Data
    @lombok.AllArgsConstructor
    class RangoFechas {
        LocalDate fechaInicio;
        LocalDate fechaFin;
    }
}