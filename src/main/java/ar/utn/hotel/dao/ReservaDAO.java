package ar.utn.hotel.dao;

import ar.utn.hotel.dto.ReservaDTO;
import java.util.List;

public interface ReservaDAO {
    ReservaDTO findById(Long id);
    List<ReservaDTO> findAll();
    void create(ReservaDTO reserva);
    void update(ReservaDTO reserva);
    void delete(Long id);
}
