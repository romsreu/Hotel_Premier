package ar.utn.hotel.services;

import ar.utn.hotel.dao.*;
import ar.utn.hotel.dao.impl.*;
import ar.utn.hotel.model.*;
import enums.EstadoHab;

import java.time.LocalDate;
import java.util.*;

/**
 * Inicializador SIMPLIFICADO - Solo crea la estructura básica del sistema
 */
public class InicializadorSimple {

    private final HabitacionDAO habitacionDAO;
    private final TipoHabitacionDAO tipoHabitacionDAO;
    private final EstadoHabitacionDAO estadoHabitacionDAO;
    private final TipoEstadoDAO tipoEstadoDAO;

    private static final List<TipoHabitacionConfig> TIPOS_CONFIG = Arrays.asList(
            new TipoHabitacionConfig("Individual Estándar",
                    "Habitación individual con cama simple", 1, 80.0, 10),
            new TipoHabitacionConfig("Doble Estándar",
                    "Habitación doble con dos camas individuales o una matrimonial", 2, 120.0, 18),
            new TipoHabitacionConfig("Doble Superior",
                    "Habitación doble amplia con amenities premium", 2, 150.0, 8),
            new TipoHabitacionConfig("Superior Family Plan",
                    "Habitación familiar con espacio adicional", 4, 200.0, 10),
            new TipoHabitacionConfig("Suite Doble",
                    "Suite de lujo con sala de estar separada", 2, 300.0, 2)
    );

    public InicializadorSimple() {
        this.tipoEstadoDAO = new TipoEstadoDAOImpl();
        this.estadoHabitacionDAO = new EstadoHabitacionDAOImpl();
        this.habitacionDAO = new HabitacionDAOImpl(tipoEstadoDAO);
        this.tipoHabitacionDAO = new TipoHabitacionDAOImpl();
    }

    public void inicializar() {
        System.out.println("=== Iniciando carga de datos básicos ===");

        try {
            // 1. Crear tipos de estado
            inicializarCatalogoEstados();

            // 2. Crear tipos de habitación
            inicializarTiposHabitacion();

            // 3. Crear habitaciones
            inicializarHabitaciones();

            System.out.println("\n=== Carga completada exitosamente ===");
            mostrarResumen();

        } catch (Exception e) {
            System.err.println("Error durante la inicialización: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void inicializarCatalogoEstados() {
        System.out.println("\n--- Inicializando catálogo de estados ---");

        for (EstadoHab estadoEnum : EstadoHab.values()) {
            if (!tipoEstadoDAO.existeEstado(estadoEnum)) {
                TipoEstado tipoEstado = TipoEstado.builder()
                        .estado(estadoEnum)
                        .build();

                tipoEstadoDAO.guardar(tipoEstado);
                System.out.println("✓ Estado creado: " + estadoEnum.name());
            } else {
                System.out.println("○ Estado ya existe: " + estadoEnum.name());
            }
        }
    }

    private void inicializarTiposHabitacion() {
        System.out.println("\n--- Inicializando tipos de habitación ---");

        for (TipoHabitacionConfig config : TIPOS_CONFIG) {
            TipoHabitacion tipoExistente = tipoHabitacionDAO.buscarPorNombre(config.nombre);

            if (tipoExistente == null) {
                TipoHabitacion tipo = TipoHabitacion.builder()
                        .nombre(config.nombre)
                        .descripcion(config.descripcion)
                        .capacidad(config.capacidad)
                        .costoNoche(config.costoNoche)
                        .habitaciones(new HashSet<>())
                        .build();

                tipoHabitacionDAO.guardar(tipo);
                System.out.println("✓ Tipo creado: " + config.nombre + " - $" + config.costoNoche + "/noche");
            } else {
                System.out.println("○ Tipo ya existe: " + config.nombre);
            }
        }
    }

    private void inicializarHabitaciones() {
        System.out.println("\n--- Inicializando habitaciones ---");

        TipoEstado estadoDisponible = tipoEstadoDAO.buscarPorEstado(EstadoHab.DISPONIBLE);
        if (estadoDisponible == null) {
            throw new IllegalStateException("No se encontró el estado DISPONIBLE en el catálogo");
        }

        int pisoActual = 1;
        int habitacionesPorPiso = 24;
        int habitacionesEnPisoActual = 0;

        for (TipoHabitacionConfig config : TIPOS_CONFIG) {
            TipoHabitacion tipo = tipoHabitacionDAO.buscarPorNombre(config.nombre);

            if (tipo == null) {
                System.err.println("ERROR: No se encontró el tipo " + config.nombre);
                continue;
            }

            System.out.println("\nCreando " + config.cantidad + " habitaciones de tipo: " + tipo.getDescripcion());

            for (int i = 0; i < config.cantidad; i++) {
                Integer numeroHabitacion = Integer.valueOf(
                        String.format("%d%02d", pisoActual, (habitacionesEnPisoActual % habitacionesPorPiso) + 1)
                );

                if (!habitacionDAO.existeNumero(numeroHabitacion)) {
                    Habitacion habitacion = Habitacion.builder()
                            .numero(numeroHabitacion)
                            .tipo(tipo)
                            .piso(pisoActual)
                            .estados(new HashSet<>())
                            .reservas(new HashSet<>())
                            .estadias(new HashSet<>())
                            .build();

                    habitacionDAO.guardar(habitacion);

                    EstadoHabitacion estadoInicial = EstadoHabitacion.builder()
                            .habitacion(habitacion)
                            .tipoEstado(estadoDisponible)
                            .fechaDesde(LocalDate.now().minusMonths(1))
                            .fechaHasta(null)
                            .build();

                    estadoHabitacionDAO.guardar(estadoInicial);

                    System.out.println("✓ Habitación " + numeroHabitacion + " creada - " + tipo.getDescripcion());
                } else {
                    System.out.println("○ Habitación " + numeroHabitacion + " ya existe - omitida");
                }

                habitacionesEnPisoActual++;

                if (habitacionesEnPisoActual >= habitacionesPorPiso) {
                    pisoActual++;
                    habitacionesEnPisoActual = 0;
                }
            }
        }
    }

    private void mostrarResumen() {
        System.out.println("\n=== RESUMEN ===");

        List<TipoHabitacion> tipos = tipoHabitacionDAO.listarTodos();

        for (TipoHabitacion tipo : tipos) {
            Long cantidad = habitacionDAO.contarPorTipo(tipo);
            System.out.printf("%-30s: %2d habitaciones - $%.2f/noche (Cap: %d)%n",
                    tipo.getDescripcion(),
                    cantidad,
                    tipo.getCostoNoche(),
                    tipo.getCapacidad());
        }

        System.out.println("\nTotal tipos de habitación: " + tipos.size());
        System.out.println("Total de habitaciones: " + habitacionDAO.listarTodas().size());

        System.out.println("\nHabitaciones por Estado:");
        for (EstadoHab estado : EstadoHab.values()) {
            long count = habitacionDAO.buscarPorEstado(estado).size();
            System.out.println("  " + estado.name() + ": " + count);
        }
    }

    private static class TipoHabitacionConfig {
        String nombre;
        String descripcion;
        Integer capacidad;
        Double costoNoche;
        int cantidad;

        TipoHabitacionConfig(String nombre, String descripcion,
                             Integer capacidad, Double costoNoche, int cantidad) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.capacidad = capacidad;
            this.costoNoche = costoNoche;
            this.cantidad = cantidad;
        }
    }

    public static void main(String[] args) {
        System.out.println("🏨 INICIALIZADOR SIMPLE - Solo estructura básica");
        System.out.println("================================================\n");

        InicializadorSimple inicializador = new InicializadorSimple();
        inicializador.inicializar();

        System.out.println("\n================================================");
        System.out.println("✅ Sistema listo para usar");
        System.out.println("📝 Ahora puedes:");
        System.out.println("   1. Dar de alta huéspedes desde la interfaz");
        System.out.println("   2. Crear reservas");
        System.out.println("   3. Ocupar habitaciones");
        System.out.println("================================================");
    }
}