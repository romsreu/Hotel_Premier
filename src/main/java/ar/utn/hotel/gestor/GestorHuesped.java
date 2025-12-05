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

    // Constructor por defecto
    public GestorHuesped() {
        this.direccionDAO = new DireccionDAOImpl();
        this.huespedDAO = new HuespedDAOImpl();
    }

    // Constructor para inyección de dependencias
    public GestorHuesped(DireccionDAO direccionDAO, HuespedDAO huespedDAO) {
        this.direccionDAO = direccionDAO;
        this.huespedDAO = huespedDAO;
    }

    /**
     * Da de alta un nuevo huésped en el sistema
     */
    public Huesped cargar(DarAltaHuespedDTO dto) {
        // Verificar si ya existe
        if (huespedDAO.existePorDocumento(dto.getNumeroDocumento(), dto.getTipoDocumento())) {
            throw new IllegalArgumentException(
                    "Ya existe un huésped registrado con el documento " +
                            dto.getTipoDocumento() + " " + dto.getNumeroDocumento()
            );
        }

        // Buscar o crear Dirección
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

        // Crear Huesped
        Huesped huesped = new Huesped();
        huesped.setNombre(dto.getNombre());
        huesped.setApellido(dto.getApellido());
        huesped.setTelefono(dto.getTelefono());
        huesped.setDireccion(dir);
        huesped.setNumeroDocumento(dto.getNumeroDocumento());
        huesped.setTipoDocumento(dto.getTipoDocumento());
        huesped.setPosicionIVA(dto.getPosicionIVA());
        huesped.setFechaNacimiento(dto.getFechaNacimiento());
        huesped.setOcupacion(dto.getOcupacion());
        huesped.setNacionalidad(dto.getNacionalidad());
        huesped.setEmail(dto.getEmail());
        huesped.setCuit(dto.getCuit());

        return huespedDAO.guardar(huesped);
    }

    /**
     * Busca huéspedes según criterios
     */
    public List<Huesped> buscarHuesped(BuscarHuespedDTO dto) {
        return huespedDAO.buscarHuesped(dto);
    }


    /**
     * Busca huéspedes por nombre y apellido (para ocupar habitación)
     */
    public List<Huesped> buscarPorNombreApellido(String nombre, String apellido) {
        BuscarHuespedDTO dto = new BuscarHuespedDTO();
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        return buscarHuesped(dto);
    }

    /**
     * Busca huéspedes por nombre, apellido y DNI
     */
    public List<Huesped> buscarPorNombreApellidoDNI(String nombre, String apellido, String numeroDocumento) {
        BuscarHuespedDTO dto = new BuscarHuespedDTO();
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setNumeroDocumento(numeroDocumento);
        return buscarHuesped(dto);
    }

    /**
     * Busca huéspedes o acompañantes por nombre y apellido
     * (sin requerir DNI, útil para acompañantes)
     */
    public List<Huesped> buscarPersonaPorNombreApellido(String nombre, String apellido) {
        BuscarHuespedDTO dto = new BuscarHuespedDTO();
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        // No se establece DNI, por lo que busca solo por nombre y apellido
        return buscarHuesped(dto);
    }
}