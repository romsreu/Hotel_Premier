package ar.utn.hotel.services;

import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import enums.EstadoHabitacion;
import enums.TipoHabitacion;
import ar.utn.hotel.model.Habitacion;
import java.util.HashMap;
import java.util.Map;

public class InicializadorHabitaciones {

    private final HabitacionDAO habitacionDAO;

    // Configuración según la tabla
    private static final Map<TipoHabitacion, Integer> CANTIDAD_POR_TIPO = new HashMap<>();

    static {
        CANTIDAD_POR_TIPO.put(TipoHabitacion.INDIVIDUAL_ESTANDAR, 10);
        CANTIDAD_POR_TIPO.put(TipoHabitacion.DOBLE_ESTANDAR, 18);
        CANTIDAD_POR_TIPO.put(TipoHabitacion.DOBLE_SUPERIOR, 8);
        CANTIDAD_POR_TIPO.put(TipoHabitacion.SUPERIOR_FAMILY_PLAN, 10);
        CANTIDAD_POR_TIPO.put(TipoHabitacion.SUITE_DOBLE, 2);
    }

    public InicializadorHabitaciones() {
        this.habitacionDAO = new HabitacionDAOImpl();
    }

    public InicializadorHabitaciones(HabitacionDAO habitacionDAO) {
        this.habitacionDAO = habitacionDAO;
    }

    // Inicializa todas las habitaciones en la base de datos
    public void inicializar() {
        System.out.println("=== Iniciando carga de habitaciones ===");

        int contador = 1;
        int pisoActual = 1;
        int habitacionesPorPiso = 24;
        int habitacionesEnPisoActual = 0;

        for (Map.Entry<TipoHabitacion, Integer> entry : CANTIDAD_POR_TIPO.entrySet()) {
            TipoHabitacion tipo = entry.getKey();
            int cantidad = entry.getValue();

            System.out.println("\nCreando " + cantidad + " habitaciones de tipo: " + tipo.getDescripcion());

            for (int i = 0; i < cantidad; i++) {
                // Generar número de habitación (formato: PISO + NÚMERO)
                String numeroHabitacion = String.format("%d%02d", pisoActual, (habitacionesEnPisoActual % habitacionesPorPiso) + 1);

                // Verificar si ya existe
                if (!habitacionDAO.existeNumero(numeroHabitacion)) {
                    Habitacion habitacion = Habitacion.builder()
                            .numero(Integer.valueOf(numeroHabitacion))
                            .tipo(tipo)
                            .costoNoche(tipo.getCostoNoche())
                            .estado(EstadoHabitacion.DISPONIBLE)
                            .piso(pisoActual)
                            .build();

                    habitacionDAO.guardar(habitacion);
                    System.out.println("✓ Habitación " + numeroHabitacion + " creada - " + tipo.getDescripcion());
                } else {
                    System.out.println("○ Habitación " + numeroHabitacion + " ya existe - omitida");
                }

                contador++;
                habitacionesEnPisoActual++;

                // Cambiar de piso cada 24 habitaciones
                if (habitacionesEnPisoActual >= habitacionesPorPiso) {
                    pisoActual++;
                    habitacionesEnPisoActual = 0;
                }
            }
        }

        System.out.println("\n=== Carga de habitaciones completada ===");
        mostrarResumen();
    }

    // Elimina todas las habitaciones y vuelve a inicializar
    public void reinicializar() {
        System.out.println("Eliminando todas las habitaciones...");

        for (Habitacion habitacion : habitacionDAO.listarTodas()) {
            habitacionDAO.eliminar(habitacion.getNumero());
        }

        System.out.println("Todas las habitaciones eliminadas");
        inicializar();
    }

    // Muestra un resumen de las habitaciones cargadas
    public void mostrarResumen() {
        System.out.println("\n=== RESUMEN DE HABITACIONES ===");

        for (TipoHabitacion tipo : TipoHabitacion.values()) {
            Long cantidad = habitacionDAO.contarPorTipo(tipo);
            System.out.printf("%-25s: %2d habitaciones - $%.2f/noche%n",
                    tipo.getDescripcion(),
                    cantidad,
                    tipo.getCostoNoche());
        }

        System.out.println("\nTotal de habitaciones: " + habitacionDAO.listarTodas().size());
        System.out.println("Habitaciones disponibles: " + habitacionDAO.buscarDisponibles().size());
    }
}
