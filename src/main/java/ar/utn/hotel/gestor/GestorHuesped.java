package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.HuespedDAO;
import ar.utn.hotel.dto.HuespedDTO;
import ar.utn.hotel.dto.ReservaDTO;

import java.util.List;

public class GestorHuesped {

    private final HuespedDAO huespedDAO;

    public GestorHuesped(HuespedDAO huespedDAO) {
        this.huespedDAO = huespedDAO;
    }

    public boolean registrarHuesped(HuespedDTO huesped) {
        try {
            huespedDAO.create(huesped);
            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar huésped: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarHuesped(HuespedDTO huesped) {
        try {
            huespedDAO.update(huesped);
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar huésped: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarHuesped(Long id) {
        try {
            huespedDAO.delete(id);
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar huésped: " + e.getMessage());
            return false;
        }
    }

    public HuespedDTO buscarPorDocumento(String tipoDoc, String nroDoc) {
        List<HuespedDTO> huespedes = huespedDAO.findAll();
        return huespedes.stream()
                .filter(h -> h.getTipoDocumento().equalsIgnoreCase(tipoDoc)
                        && h.getNroDocumento().equalsIgnoreCase(nroDoc))
                .findFirst()
                .orElse(null);
    }

    public List<ReservaDTO> obtenerReservasDeHuesped(Long idHuesped) {
        HuespedDTO h = huespedDAO.findById(idHuesped);
        return (h != null) ? h.getReservas() : List.of();
    }

    public List<HuespedDTO> listarHuespedes() {
        return huespedDAO.findAll();
    }
}
