package ar.utn.hotel.dao;

import ar.utn.hotel.dto.ResponsableDePagoDTO;

import java.util.List;

public interface ResponsableDePagoDAO {
    ResponsableDePagoDTO findById(Long id);
    List<ResponsableDePagoDTO> findAll();
    void create(ResponsableDePagoDTO responsable);
    void update(ResponsableDePagoDTO responsable);
    void delete(Long id);
}
