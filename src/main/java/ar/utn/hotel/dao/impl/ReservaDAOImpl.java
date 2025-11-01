package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dto.ReservaDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Transactional
public class ReservaDAOImpl implements ReservaDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void create(ReservaDTO reserva) {
        em.persist(reserva);
    }

    @Override
    public void update(ReservaDTO reserva) {
        em.merge(reserva);
    }

    @Override
    public void delete(Long id) {
        ReservaDTO r = em.find(ReservaDTO.class, id);
        if (r != null) {
            em.remove(r);
        }
    }

    @Override
    public ReservaDTO findById(Long id) {
        return em.find(ReservaDTO.class, id);
    }

    @Override
    public List<ReservaDTO> findAll() {
        return em.createQuery("SELECT r FROM ReservaDTO r", ReservaDTO.class)
                .getResultList();
    }
}
