package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.ResponsableDePagoDAO;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.dto.ResponsableDePagoDTO;

import java.util.List;

public class GestorResponsableDePago {

    private final ResponsableDePagoDAO responsableDAO;

    public GestorResponsableDePago(ResponsableDePagoDAO responsableDAO) {
        this.responsableDAO = responsableDAO;
    }

    public boolean registrarResponsable(ResponsableDePagoDTO responsable) {
        try {
            responsableDAO.create(responsable);
            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar responsable: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarResponsable(ResponsableDePagoDTO responsable) {
        try {
            responsableDAO.update(responsable);
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar responsable: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarResponsable(Long id) {
        try {
            responsableDAO.delete(id);
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar responsable: " + e.getMessage());
            return false;
        }
    }

    public ResponsableDePagoDTO obtenerPorId(Long id) {
        return responsableDAO.findById(id);
    }

    public List<ReservaDTO> obtenerReservasDeResponsable(Long idResponsable) {
        ResponsableDePagoDTO r = responsableDAO.findById(idResponsable);
        return (r != null) ? r.getReservas() : List.of();
    }

    public List<ResponsableDePagoDTO> listarResponsables() {
        return responsableDAO.findAll();
    }
}
