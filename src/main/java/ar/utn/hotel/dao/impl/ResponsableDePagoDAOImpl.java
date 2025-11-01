package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.ResponsableDePagoDAO;
import ar.utn.hotel.dto.ResponsableDePagoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Transactional
public class ResponsableDePagoDAOImpl implements ResponsableDePagoDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void create(ResponsableDePagoDTO responsable) {
        em.persist(responsable);
    }

    @Override
    public void update(ResponsableDePagoDTO responsable) {
        em.merge(responsable);
    }

    @Override
    public void delete(Long id) {
        ResponsableDePagoDTO r = em.find(ResponsableDePagoDTO.class, id);
        if (r != null) {
            em.remove(r);
        }
    }

    @Override
    public ResponsableDePagoDTO findById(Long id) {
        return em.find(ResponsableDePagoDTO.class, id);
    }

    @Override
    public List<ResponsableDePagoDTO> findAll() {
        return em.createQuery("SELECT r FROM ResponsableDePagoDTO r", ResponsableDePagoDTO.class)
                .getResultList();
    }
}
