package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.HuespedDAO;
import ar.utn.hotel.dto.HuespedDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
public class HuespedDAOImpl implements HuespedDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void create(HuespedDTO huesped) {
        em.persist(huesped);
    }

    @Override
    public void update(HuespedDTO huesped) {
        em.merge(huesped);
    }

    @Override
    public void delete(Long id) {
        HuespedDTO h = em.find(HuespedDTO.class, id);
        if (h != null) {
            em.remove(h);
        }
    }

    @Override
    public HuespedDTO findById(Long id) {
        return em.find(HuespedDTO.class, id);
    }

    @Override
    public List<HuespedDTO> findAll() {
        return em.createQuery("SELECT h FROM HuespedDTO h", HuespedDTO.class)
                .getResultList();
    }
}
