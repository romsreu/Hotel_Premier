package ar.utn.hotel.dao;

import ar.utn.hotel.dto.HabitacionDTO;
import java.util.List;

public interface HabitacionDAO {
    HabitacionDTO findById(Long id);
    List<HabitacionDTO> findAll();
    void create(HabitacionDTO habitacion);
    void update(HabitacionDTO habitacion);
    void delete(Long id);
}