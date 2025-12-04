package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import ar.utn.hotel.dao.impl.PersonaDAOImpl;
import ar.utn.hotel.dao.impl.ReservaDAOImpl;
import ar.utn.hotel.dto.HabitacionReservaDTO;
import ar.utn.hotel.model.Huesped;
import ar.utn.hotel.model.Reserva;

import java.util.*;

public class GestorOcupacion {

    private final GestorHabitacion gestorHabitacion;
    private final GestorReserva gestorReserva;
    private final ReservaDAOImpl reservaDAO;
    private final HabitacionDAOImpl habitacionDAO;

    public GestorOcupacion() {
        EstadoHabitacionDAOImpl estadoDAO = new EstadoHabitacionDAOImpl();
        PersonaDAOImpl personaDAO = new PersonaDAOImpl();

        this.habitacionDAO = new HabitacionDAOImpl(estadoDAO);
        this.reservaDAO = new ReservaDAOImpl(personaDAO, estadoDAO);
        this.gestorHabitacion = new GestorHabitacion(habitacionDAO, null);
        this.gestorReserva = new GestorReserva(reservaDAO, personaDAO);
    }

    /**
     * Ocupa habitaciones y asocia el huésped
     * - Si existe reserva: Solo cambia estado a OCUPADA
     * - Si NO existe reserva: Crea reserva Y ocupa (directo a OCUPADA)
     */
    public void ocuparHabitaciones(Huesped huesped, List<HabitacionReservaDTO> habitaciones) {
        if (huesped == null) {
            throw new IllegalArgumentException("El huésped no puede ser nulo");
        }

        if (habitaciones == null || habitaciones.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos una habitación");
        }

        try {
            for (HabitacionReservaDTO hab : habitaciones) {
                Reserva reservaExistente = gestorReserva.buscarReservaPorHabitacionYFecha(
                        hab.getNumeroHabitacion(),
                        hab.getFechaIngreso()
                );

                if (reservaExistente == null) {
                    // NO existe reserva: Crear reserva + ocupar en una sola operación
                    crearYOcupar(huesped, hab);
                } else {
                    // Ya existe reserva: Solo ocupar (cambiar estado a OCUPADA)
                    Set<Integer> habitacion = Collections.singleton(hab.getNumeroHabitacion());
                    gestorHabitacion.ocuparHabitaciones(
                            habitacion,
                            hab.getFechaIngreso(),
                            hab.getFechaEgreso()
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al ocupar habitaciones: " + e.getMessage(), e);
        }
    }

    /**
     * Crea una reserva Y ocupa la habitación (directo a OCUPADA)
     */
    private void crearYOcupar(Huesped huesped, HabitacionReservaDTO hab) {
        // 1. Crear la reserva (esto pone estado RESERVADA)
        Map<Integer, ReservaDAO.RangoFechas> habitacionSola = new HashMap<>();
        habitacionSola.put(
                hab.getNumeroHabitacion(),
                new ReservaDAO.RangoFechas(hab.getFechaIngreso(), hab.getFechaEgreso())
        );

        try {
            reservaDAO.crearReservas(huesped.getId(), habitacionSola);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear reserva: " + e.getMessage(), e);
        }

        // 2. Inmediatamente ocupar (cambiar estado a OCUPADA)
        Set<Integer> habitacion = Collections.singleton(hab.getNumeroHabitacion());
        gestorHabitacion.ocuparHabitaciones(
                habitacion,
                hab.getFechaIngreso(),
                hab.getFechaEgreso()
        );
    }
}
