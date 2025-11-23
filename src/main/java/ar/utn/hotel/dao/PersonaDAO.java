package ar.utn.hotel.dao;

import ar.utn.hotel.model.Persona;
import java.util.List;

public interface PersonaDAO {

    Persona obtenerPorId(Long id);

    Persona buscarPorNombreApellido(String nombre, String apellido);

    Persona buscarPorTelefono(String telefono);

    List<Persona> obtenerTodas();

    Persona guardar(Persona persona);

    void actualizar(Persona persona);

    void eliminar(Long id);
}