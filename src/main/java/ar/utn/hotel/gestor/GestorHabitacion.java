package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dto.HabitacionDTO;
import ar.utn.hotel.dto.ReservaDTO;

import java.util.List;

public class GestorHabitacion {

    private final HabitacionDAO habitacionDAO;

    public GestorHabitacion(HabitacionDAO habitacionDAO) {
        this.habitacionDAO = habitacionDAO;
    }

    public boolean ocuparHabitacion(HabitacionDTO hab) {
        if (hab.isOcupada()) return false;
        hab.setOcupada(true);
        habitacionDAO.update(hab);
        return true;
    }

    public List<HabitacionDTO> mostrarEstadoHabitaciones() {
        return habitacionDAO.findAll();
    }

    public boolean reservarHabitacion(ReservaDTO reserva, HabitacionDTO hab) {
        if (hab.isOcupada()) return false;
        hab.setReserva(reserva);
        hab.setOcupada(true);
        habitacionDAO.update(hab);
        return true;
    }
}