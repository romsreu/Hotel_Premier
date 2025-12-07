package ar.utn.hotel.dao.implement;

import ar.utn.hotel.dao.interfaces.ReservaDAO;
import ar.utn.hotel.dao.interfaces.TipoEstadoDAO;
import ar.utn.hotel.dto.CrearReservaDTO;
import ar.utn.hotel.model.*;
import enums.EstadoHab;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.time.LocalDate;
import java.util.List;

public class ReservaDAOImpl implements ReservaDAO {

    private final TipoEstadoDAO tipoEstadoDAO;

    public ReservaDAOImpl(TipoEstadoDAO tipoEstadoDAO) {
        this.tipoEstadoDAO = tipoEstadoDAO;
    }

    @Override
    public Reserva crearReserva(CrearReservaDTO dto) {
        Transaction transaction = null;
        Session session = null;

        try {
            // 1. Abrimos la sesión manualmente
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // --- VALIDACIONES ---

            // A. Verificar Huesped
            Huesped huesped = session.get(Huesped.class, dto.getIdHuesped());
            if (huesped == null) {
                throw new RuntimeException("Error: El huésped no se encuentra registrado en el sistema.");
            }

            // B. Obtener Habitación
            Habitacion habitacion = session.createQuery(
                            "SELECT DISTINCT h FROM Habitacion h " +
                                    "LEFT JOIN FETCH h.estados " +
                                    "WHERE h.numero = :numero",
                            Habitacion.class)
                    .setParameter("numero", dto.getNumeroHabitacion())
                    .uniqueResult();

            if (habitacion == null) {
                throw new IllegalArgumentException("La habitación número " + dto.getNumeroHabitacion() + " no existe");
            }

            // C. Obtener Tipo Estado
            TipoEstado tipoReservada = tipoEstadoDAO.buscarPorEstado(EstadoHab.RESERVADA);
            if (tipoReservada == null) {
                throw new IllegalStateException("No existe el tipo estado RESERVADA en el catálogo");
            }

            // D. NUEVA VALIDACIÓN: Verificar solapamiento de fechas
            // Consultamos si existe alguna reserva para esta habitación que choque con las fechas solicitadas
            Long coincidencias = session.createQuery(
                            "SELECT COUNT(r) FROM Reserva r " +
                                    "WHERE r.habitacion.id = :idHabitacion " +
                                    "AND r.fechaInicio < :nuevaFechaFin " +  // Lógica de intersección de fechas
                                    "AND r.fechaFin > :nuevaFechaInicio",     // Lógica de intersección de fechas
                            Long.class)
                    .setParameter("idHabitacion", habitacion.getNumero())
                    .setParameter("nuevaFechaInicio", dto.getFechaInicio())
                    .setParameter("nuevaFechaFin", dto.getFechaFin())
                    .uniqueResult();

            if (coincidencias > 0) {
                throw new IllegalStateException(
                        "La habitación " + dto.getNumeroHabitacion() +
                                " ya se encuentra reservada en el rango de fechas seleccionado."
                );
            }

            // --- PERSISTENCIA ---

            // 1. Crear el registro en EstadosHabitaciones (Historial)
            // NOTA: No modificamos el "estado actual" anterior para no romper la disponibilidad de hoy
            // si la reserva es a futuro. Solo insertamos el nuevo período ocupado.
            EstadoHabitacion nuevoEstado = EstadoHabitacion.builder()
                    .habitacion(habitacion)
                    .tipoEstado(tipoReservada)
                    .fechaDesde(dto.getFechaInicio())
                    .fechaHasta(dto.getFechaFin())
                    .build();

            habitacion.getEstados().add(nuevoEstado);
            session.persist(nuevoEstado);

            // 2. Crear la Reserva
            Reserva reserva = Reserva.builder()
                    .huesped(huesped)
                    .habitacion(habitacion)
                    .fechaInicio(dto.getFechaInicio())
                    .fechaFin(dto.getFechaFin())
                    .cantHuespedes(dto.getCantHuespedes())
                    .descuento(dto.getDescuento())
                    .build();

            session.persist(reserva);

            transaction.commit();
            return reserva;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            // Imprimimos el error para debug
            System.err.println("ERROR EN DAO CREAR RESERVA: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al crear reserva: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public Reserva obtenerPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion h " +
                                    "LEFT JOIN FETCH h.estados " +
                                    "LEFT JOIN FETCH r.huesped " +
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
                                    "LEFT JOIN FETCH r.huesped " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorHuesped(Long idHuesped) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion " +
                                    "WHERE r.huesped.id = :idHuesped " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .setParameter("idHuesped", idHuesped)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion " +
                                    "LEFT JOIN FETCH r.huesped " +
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
                                    "LEFT JOIN FETCH r.huesped " +
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
                // Al eliminar reserva, liberamos el historial de estado asociado a "hoy" si corresponde
                TipoEstado tipoDisponible = tipoEstadoDAO.buscarPorEstado(EstadoHab.DISPONIBLE);

                if (tipoDisponible != null) {
                    Habitacion habitacion = reserva.getHabitacion();

                    // Lógica simplificada para volver a disponible
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