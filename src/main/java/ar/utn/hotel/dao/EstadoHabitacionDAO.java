package ar.utn.hotel.dao;

import ar.utn.hotel.model.EstadoHabitacion;
import ar.utn.hotel.model.Habitacion;
import enums.EstadoHab;
import java.time.LocalDate;
import java.util.List;

public interface EstadoHabitacionDAO {
    EstadoHabitacion guardar(EstadoHabitacion estadoHabitacion);
    EstadoHabitacion buscarPorId(Integer id);
    List<EstadoHabitacion> listarTodos();
    List<EstadoHabitacion> listarPorHabitacion(Integer numeroHabitacion);
    List<EstadoHabitacion> listarActivos();
    List<EstadoHabitacion> listarPorTipoEstado(EstadoHab estado);
    EstadoHabitacion obtenerEstadoActual(Integer numeroHabitacion);
    EstadoHabitacion obtenerEstadoEn(Integer numeroHabitacion, LocalDate fecha);
    void actualizar(EstadoHabitacion estadoHabitacion);
    void eliminar(Integer id);
}