package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.model.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ar.utn.hotel.utils.HibernateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservaDAOImpl implements ReservaDAO {

    private final PersonaDAO personaDAO;
    private final EstadoHabitacionDAO estadoHabitacionDAO;

    public ReservaDAOImpl(PersonaDAO personaDAO, EstadoHabitacionDAO estadoHabitacionDAO) {
        this.personaDAO = personaDAO;
        this.estadoHabitacionDAO = estadoHabitacionDAO;
    }

    @Override
    public List<Reserva> crearReservas(Long idPersona, Map<Integer, RangoFechas> habitacionesConFechas) {
        Transaction transaction = null;
        List<Reserva> reservasCreadas = new ArrayList<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Verificar que la persona existe
            Persona persona = session.get(Persona.class, idPersona);
            if (persona == null) {
                throw new RuntimeException("Error: La persona no se encuentra cargada en el sistema.");
            }

            // Obtener el estado RESERVADA del catálogo
            EstadoHabitacion estadoReservada = estadoHabitacionDAO.buscarPorEstado(
                    enums.EstadoHabitacion.RESERVADA
            );

            if (estadoReservada == null) {
                throw new IllegalStateException("No existe el estado RESERVADA en el catálogo");
            }

            // Crear UNA reserva por CADA habitación con sus fechas específicas
            for (Map.Entry<Integer, RangoFechas> entry : habitacionesConFechas.entrySet()) {
                Integer numeroHab = entry.getKey();
                RangoFechas rango = entry.getValue();

                // Obtener la habitación
                Habitacion habitacion = session.createQuery(
                                "SELECT DISTINCT h FROM Habitacion h " +
                                        "LEFT JOIN FETCH h.historialEstados " +
                                        "WHERE h.numero = :numero",
                                Habitacion.class)
                        .setParameter("numero", numeroHab)
                        .uniqueResult();

                if (habitacion == null) {
                    throw new IllegalArgumentException("La habitación número " + numeroHab + " no existe");
                }

                // Verificar disponibilidad
                RegistroEstadoHabitacion registroActual = habitacion.getEstadoActual();
                if (registroActual != null &&
                        registroActual.getEstado().getEstado() != enums.EstadoHabitacion.DISPONIBLE) {
                    throw new IllegalStateException(
                            "La habitación número " + numeroHab + " no está disponible. " +
                                    "Estado actual: " + registroActual.getEstado().getEstado()
                    );
                }

                // Cerrar el registro actual
                if (registroActual != null) {
                    registroActual.setFechaHasta(rango.getFechaInicio().minusDays(1));
                    session.merge(registroActual);
                }

                // Crear nuevo registro con estado RESERVADA
                RegistroEstadoHabitacion nuevoRegistro = RegistroEstadoHabitacion.builder()
                        .habitacion(habitacion)
                        .estado(estadoReservada)
                        .fechaDesde(rango.getFechaInicio())
                        .fechaHasta(rango.getFechaFin())
                        .build();

                habitacion.getHistorialEstados().add(nuevoRegistro);
                session.persist(nuevoRegistro);

                // Crear UNA reserva para ESTA habitación con SUS fechas específicas
                Reserva reserva = Reserva.builder()
                        .persona(persona)
                        .habitacion(habitacion)
                        .fechaInicio(rango.getFechaInicio())
                        .fechaFin(rango.getFechaFin())
                        .build();

                // Persistir la reserva
                session.persist(reserva);
                reservasCreadas.add(reserva);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al crear reservas: " + e.getMessage(), e);
        }

        return reservasCreadas;
    }

    @Override
    public List<Reserva> crearReservasPorNombreApellido(String nombre, String apellido,
                                                        Map<Integer, RangoFechas> habitacionesConFechas) {
        Persona persona = personaDAO.buscarPorNombreApellido(nombre, apellido);

        if (persona == null) {
            throw new RuntimeException("Error: La persona no se encuentra cargada en el sistema.");
        }

        return crearReservas(persona.getId(), habitacionesConFechas);
    }

    @Override
    public List<Reserva> crearReservasPorTelefono(String telefono,
                                                  Map<Integer, RangoFechas> habitacionesConFechas) {
        Persona persona = personaDAO.buscarPorTelefono(telefono);

        if (persona == null) {
            throw new RuntimeException("Error: La persona no se encuentra cargada en el sistema.");
        }

        return crearReservas(persona.getId(), habitacionesConFechas);
    }

    @Override
    public Reserva obtenerPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion h " +
                                    "LEFT JOIN FETCH h.historialEstados " +
                                    "LEFT JOIN FETCH r.persona " +
                                    "WHERE r.id = :id",
                            Reserva.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    @Override
    public List<Reserva> obtenerTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion " +
                                    "LEFT JOIN FETCH r.persona " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorPersona(Long idPersona) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion " +
                                    "WHERE r.persona.id = :idPersona " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .setParameter("idPersona", idPersona)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion " +
                                    "LEFT JOIN FETCH r.persona " +
                                    "WHERE r.fechaInicio <= :fechaFin " +
                                    "AND r.fechaFin >= :fechaInicio " +
                                    "ORDER BY r.fechaInicio",
                            Reserva.class)
                    .setParameter("fechaInicio", fechaInicio)
                    .setParameter("fechaFin", fechaFin)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorHabitacion(Integer numeroHabitacion) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.persona " +
                                    "WHERE r.habitacion.numero = :numeroHabitacion " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .setParameter("numeroHabitacion", numeroHabitacion)
                    .getResultList();
        }
    }

    @Override
    public void actualizar(Reserva reserva) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(reserva);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Reserva reserva = session.get(Reserva.class, id);
            if (reserva != null) {
                // Liberar la habitación (cambiar estado a DISPONIBLE)
                EstadoHabitacion estadoDisponible = estadoHabitacionDAO.buscarPorEstado(
                        enums.EstadoHabitacion.DISPONIBLE
                );

                if (estadoDisponible != null) {
                    Habitacion habitacion = reserva.getHabitacion();

                    RegistroEstadoHabitacion registroActual = habitacion.getEstadoActual();
                    if (registroActual != null) {
                        registroActual.setFechaHasta(LocalDate.now());
                        session.merge(registroActual);
                    }

                    RegistroEstadoHabitacion nuevoRegistro = RegistroEstadoHabitacion.builder()
                            .habitacion(habitacion)
                            .estado(estadoDisponible)
                            .fechaDesde(LocalDate.now())
                            .build();

                    habitacion.getHistorialEstados().add(nuevoRegistro);
                    session.persist(nuevoRegistro);
                }

                session.remove(reserva);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar reserva: " + e.getMessage(), e);
        }
    }
}