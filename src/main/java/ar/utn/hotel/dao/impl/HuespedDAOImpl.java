package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.HuespedDAO;
import ar.utn.hotel.dto.BuscarHuespedDTO;
import ar.utn.hotel.model.Huesped;
import ar.utn.hotel.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class HuespedDAOImpl implements HuespedDAO {

    @Override
    public Huesped guardar(Huesped huesped) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.persist(huesped);
            tx.commit();
            return huesped;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    @Override
    public boolean existePorDocumento(String numeroDocumento, String tipoDocumento) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Long count = s.createQuery("""
                    SELECT COUNT(h) FROM Huesped h
                    WHERE h.numeroDocumento = :numDoc
                      AND h.tipoDocumento = :tipoDoc
                    """, Long.class)
                    .setParameter("numDoc", numeroDocumento)
                    .setParameter("tipoDoc", tipoDocumento)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    @Override
    public List<Huesped> buscarHuesped(BuscarHuespedDTO dto) {
        boolean nombreVacio = dto.getNombre() == null || dto.getNombre().trim().isEmpty();
        boolean apellidoVacio = dto.getApellido() == null || dto.getApellido().trim().isEmpty();
        boolean documentoVacio = dto.getNumeroDocumento() == null || dto.getNumeroDocumento().trim().isEmpty();

        if (nombreVacio && apellidoVacio && documentoVacio) {
            return new ArrayList<>();
        }

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT h FROM Huesped h WHERE 1=1");

            if (!nombreVacio) {
                hql.append(" AND LOWER(h.nombre) LIKE LOWER(:nombre)");
            }
            if (!apellidoVacio) {
                hql.append(" AND LOWER(h.apellido) LIKE LOWER(:apellido)");
            }
            if (!documentoVacio) {
                hql.append(" AND h.numeroDocumento = :numDoc");
            }

            var query = s.createQuery(hql.toString(), Huesped.class);

            if (!nombreVacio) {
                query.setParameter("nombre", "%" + dto.getNombre().trim() + "%");
            }
            if (!apellidoVacio) {
                query.setParameter("apellido", "%" + dto.getApellido().trim() + "%");
            }
            if (!documentoVacio) {
                query.setParameter("numDoc", dto.getNumeroDocumento().trim());
            }

            return query.getResultList();
        }
    }
}