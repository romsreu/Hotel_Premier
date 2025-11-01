package ar.utn.hotel.dao;

import ar.utn.hotel.dto.HuespedDTO;
import java.util.List;

public interface HuespedDAO {
    HuespedDTO findById(Long id);
    List<HuespedDTO> findAll();
    void create(HuespedDTO huesped);
    void update(HuespedDTO huesped);
    void delete(Long id);
}
