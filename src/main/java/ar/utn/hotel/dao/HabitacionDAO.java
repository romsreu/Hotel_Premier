package ar.utn.hotel.dao;

import enums.EstadoHabitacion;
import enums.TipoHabitacion;
import ar.utn.hotel.model.Habitacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface HabitacionDAO {

    Habitacion guardar(Habitacion habitacion);

    Habitacion buscarPorNumero(Integer numero);

    List<Habitacion> listarTodas();

    List<Habitacion> listarPorRangoDeFechas(LocalDate fechaInicio, LocalDate fechaFin);

    List<Habitacion> buscarPorTipo(TipoHabitacion tipo);

    List<Habitacion> buscarPorEstado(EstadoHabitacion estado);

    List<Habitacion> buscarDisponibles();

    void actualizar(Habitacion habitacion);

    void eliminar(Integer id);

    boolean existeNumero(String numero);

    Long contarPorTipo(TipoHabitacion tipo);

    void reservarHabitaciones(Set<Integer> numerosHabitaciones);
}