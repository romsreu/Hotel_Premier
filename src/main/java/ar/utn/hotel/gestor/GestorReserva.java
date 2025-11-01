package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dto.HabitacionDTO;
import ar.utn.hotel.dto.HuespedDTO;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.dto.ResponsableDePagoDTO;

import java.util.List;

public class GestorReserva {

    private final ReservaDAO reservaDAO;
    private final HabitacionDAO habitacionDAO;

    public GestorReserva(ReservaDAO reservaDAO, HabitacionDAO habitacionDAO) {
        this.reservaDAO = reservaDAO;
        this.habitacionDAO = habitacionDAO;
    }

    public boolean crearReserva(ReservaDTO reserva) {
        try {
            reservaDAO.create(reserva);
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear la reserva: " + e.getMessage());
            return false;
        }
    }

    public boolean asignarHabitacion(Long idReserva, HabitacionDTO habitacion) {
        ReservaDTO reserva = reservaDAO.findById(idReserva);
        if (reserva == null) return false;

        habitacion.setReserva(reserva);
        habitacion.setOcupada(true);

        habitacionDAO.update(habitacion);
        reserva.getHabitaciones().add(habitacion);
        reservaDAO.update(reserva);

        return true;
    }

    public boolean agregarHuesped(Long idReserva, HuespedDTO huesped) {
        ReservaDTO reserva = reservaDAO.findById(idReserva);
        if (reserva == null) return false;

        reserva.getHuespedes().add(huesped);
        reservaDAO.update(reserva);
        return true;
    }

    public boolean asignarResponsable(Long idReserva, ResponsableDePagoDTO responsable) {
        ReservaDTO reserva = reservaDAO.findById(idReserva);
        if (reserva == null) return false;

        reserva.setResponsableDePago(responsable);
        reservaDAO.update(reserva);
        return true;
    }

    public List<ReservaDTO> listarReservas() {
        return reservaDAO.findAll();
    }
}
