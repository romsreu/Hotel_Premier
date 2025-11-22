package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.DireccionDAO;
import ar.utn.hotel.dao.HuespedDAO;
import ar.utn.hotel.dao.impl.DireccionDAOImpl;
import ar.utn.hotel.dao.impl.HuespedDAOImpl;
import ar.utn.hotel.dto.BuscarHuespedDTO;
import ar.utn.hotel.dto.DarAltaHuespedDTO;
import ar.utn.hotel.model.*;

import java.util.List;

public class GestorHuesped {

    private final DireccionDAO direccionDAO;
    private final HuespedDAO huespedDAO;

    // Constructor por defecto con implementaciones concretas
    public GestorHuesped() {
        this.direccionDAO = new DireccionDAOImpl();
        this.huespedDAO = new HuespedDAOImpl();
    }

    // Constructor para inyección de dependencias (útil para testing)
    public GestorHuesped(DireccionDAO direccionDAO, HuespedDAO huespedDAO) {
        this.direccionDAO = direccionDAO;
        this.huespedDAO = huespedDAO;
    }

    public Huesped cargar(DarAltaHuespedDTO dto) {

        // 1) Verificar si ya existe un huésped con ese documento
        if (huespedDAO.existePorDocumento(dto.getNumeroDocumento(), dto.getTipoDocumento())) {
            throw new IllegalArgumentException(
                    "Ya existe un huésped registrado con el documento " +
                            dto.getTipoDocumento() + " " + dto.getNumeroDocumento()
            );
        }

        // 2) Buscar o crear Dirección
        Direccion dir = direccionDAO.buscarPorDatos(
                dto.getCalle(),
                dto.getNumero(),
                dto.getDepto(),
                dto.getPiso(),
                dto.getCodPostal(),
                dto.getLocalidad(),
                dto.getProvincia(),
                dto.getPais()
        );

        if (dir == null) {
            dir = Direccion.builder()
                    .calle(dto.getCalle())
                    .numero(dto.getNumero())
                    .departamento(dto.getDepto())
                    .piso(dto.getPiso())
                    .codPostal(dto.getCodPostal())
                    .localidad(dto.getLocalidad())
                    .provincia(dto.getProvincia())
                    .pais(dto.getPais())
                    .build();

            direccionDAO.guardar(dir);
        }

        // 3) Crear Huesped directamente (hereda de Persona)
        Huesped huesped = new Huesped();

        // Datos de Persona (clase padre)
        huesped.setNombre(dto.getNombre());
        huesped.setApellido(dto.getApellido());
        huesped.setTelefono(dto.getTelefono());
        huesped.setDireccion(dir);

        // Datos específicos de Huesped
        huesped.setNumeroDocumento(dto.getNumeroDocumento());
        huesped.setTipoDocumento(dto.getTipoDocumento());
        huesped.setPosicionIVA(dto.getPosicionIVA());
        huesped.setFechaNacimiento(dto.getFechaNacimiento());
        huesped.setOcupacion(dto.getOcupacion());
        huesped.setNacionalidad(dto.getNacionalidad());
        huesped.setEmail(dto.getEmail());
        huesped.setCuit(dto.getCuit());

        // 4) Guardar Huesped (Hibernate maneja la herencia automáticamente)
        return huespedDAO.guardar(huesped);
    }

    public List<Huesped> buscarHuesped(BuscarHuespedDTO dto) {
        // Validar que al menos un criterio esté presente
        if ((dto.getNombre() == null || dto.getNombre().trim().isEmpty()) &&
                (dto.getApellido() == null || dto.getApellido().trim().isEmpty()) &&
                (dto.getTipoDocumento() == null || dto.getTipoDocumento().trim().isEmpty()) &&
                (dto.getNumeroDocumento() == null || dto.getNumeroDocumento().trim().isEmpty())) {

            throw new IllegalArgumentException("Debe proporcionar al menos un criterio de búsqueda");
        }

        return huespedDAO.buscarHuesped(dto);
    }
}