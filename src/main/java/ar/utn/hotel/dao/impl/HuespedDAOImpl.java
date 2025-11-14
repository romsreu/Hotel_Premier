package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.HuespedDAO;
import ar.utn.hotel.model.Huesped;
import ar.utn.hotel.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
            boolean bandera = count != null && count > 0;
            if(bandera){
                System.out.println("--------");
                System.out.println("El numero de documento ya existe");
                System.out.println("--------");
            }
            return bandera;
        }
    }
}