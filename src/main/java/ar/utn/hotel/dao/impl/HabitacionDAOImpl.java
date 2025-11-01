package ar.utn.hotel.dao.impl;


import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dto.HabitacionDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Transactional
public class HabitacionDAOImpl implements HabitacionDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void create(HabitacionDTO habitacion) {
        em.persist(habitacion);
    }

    @Override
    public void update(HabitacionDTO habitacion) {
        em.merge(habitacion);
    }

    @Override
    public void delete(Long id) {
        HabitacionDTO hab = em.find(HabitacionDTO.class, id);
        if (hab != null) {
            em.remove(hab);
        }
    }

    @Override
    public HabitacionDTO findById(Long id) {
        return em.find(HabitacionDTO.class, id);
    }

    @Override
    public List<HabitacionDTO> findAll() {
        return em.createQuery("SELECT h FROM HabitacionDTO h", HabitacionDTO.class)
                .getResultList();
    }
}
