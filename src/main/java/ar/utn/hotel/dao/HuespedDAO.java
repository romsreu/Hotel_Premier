package ar.utn.hotel.dao;

import ar.utn.hotel.dto.BuscarHuespedDTO;
import ar.utn.hotel.model.Huesped;

import java.util.List;

public interface HuespedDAO {
    Huesped guardar(Huesped huesped);
    boolean existePorDocumento(String numeroDocumento, String tipoDocumento);
    List<Huesped> buscarHuesped(BuscarHuespedDTO dto);
}