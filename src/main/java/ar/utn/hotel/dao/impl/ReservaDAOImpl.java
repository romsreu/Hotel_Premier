package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dao.TipoEstadoDAO;
import ar.utn.hotel.model.*;
import enums.EstadoHab;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ar.utn.hotel.utils.HibernateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservaDAOImpl implements ReservaDAO {

    private final PersonaDAO personaDAO;
    private final TipoEstadoDAO tipoEstadoDAO;

    public ReservaDAOImpl(PersonaDAO personaDAO, TipoEstadoDAO tipoEstadoDAO) {
        this.personaDAO = personaDAO;
        this.tipoEstadoDAO = tipoEstadoDAO;
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

            // Obtener el tipo estado RESERVADA del catálogo
            TipoEstado tipoReservada = tipoEstadoDAO.buscarPorEstado(EstadoHab.RESERVADA);

            if (tipoReservada == null) {
                throw new IllegalStateException("No existe el tipo estado RESERVADA en el catálogo");
            }

            // Crear UNA reserva por CADA habitación con sus fechas específicas
            for (Map.Entry<Integer, RangoFechas> entry : habitacionesConFechas.entrySet()) {
                Integer numeroHab = entry.getKey();
                RangoFechas rango = entry.getValue();

                // Obtener la habitación
                Habitacion habitacion = session.createQuery(
                                "SELECT DISTINCT h FROM Habitacion h " +
                                        "LEFT JOIN FETCH h.estados " +
                                        "WHERE h.numero = :numero",
                                Habitacion.class)
                        .setParameter("numero", numeroHab)
                        .uniqueResult();

                if (habitacion == null) {
                    throw new IllegalArgumentException("La habitación número " + numeroHab + " no existe");
                }

                // Verificar disponibilidad
                EstadoHabitacion estadoActual = habitacion.getEstadoActual();
                if (estadoActual != null &&
                        estadoActual.getTipoEstado().getEstado() != EstadoHab.DISPONIBLE) {
                    throw new IllegalStateException(
                            "La habitación número " + numeroHab + " no está disponible. " +
                                    "Estado actual: " + estadoActual.getTipoEstado().getEstado()
                    );
                }

                // Cerrar el estado actual
                if (estadoActual != null) {
                    estadoActual.setFechaHasta(rango.getFechaInicio().minusDays(1));
                    session.merge(estadoActual);
                }

                // Crear nuevo estado con tipo RESERVADA
                EstadoHabitacion nuevoEstado = EstadoHabitacion.builder()
                        .habitacion(habitacion)
                        .tipoEstado(tipoReservada)
                        .fechaDesde(rango.getFechaInicio())
                        .fechaHasta(rango.getFechaFin())
                        .build();

                habitacion.getEstados().add(nuevoEstado);
                session.persist(nuevoEstado);

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
                                    "LEFT JOIN FETCH h.estados " +
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
                TipoEstado tipoDisponible = tipoEstadoDAO.buscarPorEstado(EstadoHab.DISPONIBLE);

                if (tipoDisponible != null) {
                    Habitacion habitacion = reserva.getHabitacion();

                    EstadoHabitacion estadoActual = habitacion.getEstadoActual();
                    if (estadoActual != null) {
                        estadoActual.setFechaHasta(LocalDate.now());
                        session.merge(estadoActual);
                    }

                    EstadoHabitacion nuevoEstado = EstadoHabitacion.builder()
                            .habitacion(habitacion)
                            .tipoEstado(tipoDisponible)
                            .fechaDesde(LocalDate.now())
                            .build();

                    habitacion.getEstados().add(nuevoEstado);
                    session.persist(nuevoEstado);
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