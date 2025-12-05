package ar.utn.hotel.services;

import ar.utn.hotel.dao.*;
import ar.utn.hotel.dao.impl.*;
import ar.utn.hotel.gestor.GestorHabitacion;
import ar.utn.hotel.gestor.GestorReserva;
import ar.utn.hotel.model.*;
import enums.EstadoHab;

import java.time.LocalDate;
import java.util.*;

/**
 * Inicializador de datos del sistema de hotel - VERSIÓN CORREGIDA
 * Solo crea: tipos de estado, tipos de habitación, habitaciones y personas
 * Las reservas y estadías se crean desde la interfaz
 */
public class InicializadorDatos {

    private final HabitacionDAO habitacionDAO;
    private final TipoHabitacionDAO tipoHabitacionDAO;
    private final EstadoHabitacionDAO estadoHabitacionDAO;
    private final TipoEstadoDAO tipoEstadoDAO;
    private final PersonaDAO personaDAO;
    private final ReservaDAO reservaDAO;
    private final EstadiaDAO estadiaDAO;

    private final GestorHabitacion gestorHabitacion;
    private final GestorReserva gestorReserva;

    // Configuración de tipos de habitación
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

    public InicializadorDatos() {
        // Inicializar DAOs
        this.tipoEstadoDAO = new TipoEstadoDAOImpl();
        this.estadoHabitacionDAO = new EstadoHabitacionDAOImpl();
        this.habitacionDAO = new HabitacionDAOImpl(tipoEstadoDAO);
        this.tipoHabitacionDAO = new TipoHabitacionDAOImpl();
        this.personaDAO = new PersonaDAOImpl();
        this.reservaDAO = new ReservaDAOImpl(personaDAO, tipoEstadoDAO);
        this.estadiaDAO = new EstadiaDAOImpl();

        // Inicializar gestores
        this.gestorHabitacion = new GestorHabitacion(
                habitacionDAO,
                tipoHabitacionDAO,
                estadoHabitacionDAO,
                tipoEstadoDAO,
                estadiaDAO,
                reservaDAO
        );

        this.gestorReserva = new GestorReserva(reservaDAO, personaDAO);

        // Establecer referencias circulares
        this.gestorReserva.setGestorHabitacion(gestorHabitacion);
        this.gestorHabitacion.setGestorReserva(gestorReserva);
    }

    /**
     * Inicializa todos los datos del sistema
     */
    public void inicializar() {
        System.out.println("=== Iniciando carga de datos ===\n");

        try {
            // 1. Crear tipos de estado (debe ser primero)
            inicializarCatalogoEstados();

            // 2. Crear tipos de habitación
            inicializarTiposHabitacion();

            // 3. Crear habitaciones
            inicializarHabitaciones();

            // 4. Crear personas (para poder hacer reservas)
            crearPersonas();

            System.out.println("\n=== Carga de datos completada exitosamente ===");
            mostrarResumen();

            System.out.println("\n📝 NOTA: Las reservas y estadías se deben crear desde la interfaz");
            System.out.println("   o usando los gestores correspondientes.\n");

        } catch (Exception e) {
            System.err.println("Error durante la inicialización: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicializa el catálogo de estados (los 4 estados del sistema)
     */
    private void inicializarCatalogoEstados() {
        System.out.println("--- Inicializando catálogo de estados ---");

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

    /**
     * Inicializa los tipos de habitación
     */
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

    /**
     * Inicializa las habitaciones distribuidas en diferentes pisos
     */
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
                // Generar número de habitación (formato: PISO + NÚMERO)
                Integer numeroHabitacion = Integer.valueOf(
                        String.format("%d%02d", pisoActual, (habitacionesEnPisoActual % habitacionesPorPiso) + 1)
                );

                // Verificar si ya existe
                if (!habitacionDAO.existeNumero(numeroHabitacion)) {
                    // Crear habitación
                    Habitacion habitacion = Habitacion.builder()
                            .numero(numeroHabitacion)
                            .tipo(tipo)
                            .piso(pisoActual)
                            .estados(new HashSet<>())
                            .reservas(new HashSet<>())
                            .estadias(new HashSet<>())
                            .build();

                    // Guardar la habitación
                    habitacionDAO.guardar(habitacion);

                    // Crear estado inicial DISPONIBLE
                    EstadoHabitacion estadoInicial = EstadoHabitacion.builder()
                            .habitacion(habitacion)
                            .tipoEstado(estadoDisponible)
                            .fechaDesde(LocalDate.now().minusMonths(1)) // Estado desde hace 1 mes
                            .fechaHasta(null) // Sin fecha fin = indefinido
                            .build();

                    estadoHabitacionDAO.guardar(estadoInicial);

                    System.out.println("✓ Habitación " + numeroHabitacion + " creada - " + tipo.getDescripcion());
                } else {
                    System.out.println("○ Habitación " + numeroHabitacion + " ya existe - omitida");
                }

                habitacionesEnPisoActual++;

                // Cambiar de piso cada 24 habitaciones
                if (habitacionesEnPisoActual >= habitacionesPorPiso) {
                    pisoActual++;
                    habitacionesEnPisoActual = 0;
                }
            }
        }
    }

    /**
     * Crea personas de ejemplo (NO huéspedes - solo para hacer reservas)
     * Una Persona solo necesita: nombre, apellido, teléfono
     */
    private void crearPersonas() {
        System.out.println("\n--- Creando personas (para poder hacer reservas) ---");

        List<Persona> personas = Arrays.asList(
                Persona.builder()
                        .nombre("Juan")
                        .apellido("Pérez")
                        .telefono("3511234567")
                        .build(),

                Persona.builder()
                        .nombre("María")
                        .apellido("González")
                        .telefono("3512345678")
                        .build(),

                Persona.builder()
                        .nombre("Carlos")
                        .apellido("Rodríguez")
                        .telefono("3513456789")
                        .build(),

                Persona.builder()
                        .nombre("Ana")
                        .apellido("Martínez")
                        .telefono("3514567890")
                        .build(),

                Persona.builder()
                        .nombre("Pedro")
                        .apellido("López")
                        .telefono("3515678901")
                        .build(),

                Persona.builder()
                        .nombre("Laura")
                        .apellido("Fernández")
                        .telefono("3516789012")
                        .build(),

                Persona.builder()
                        .nombre("José")
                        .apellido("Rodríguez")
                        .telefono("3517890123")
                        .build()
        );

        for (Persona persona : personas) {
            try {
                Persona existente = personaDAO.buscarPorNombreApellido(
                        persona.getNombre(),
                        persona.getApellido()
                );

                if (existente == null) {
                    personaDAO.guardar(persona);
                    System.out.println("✓ Persona creada: " + persona.getNombre() + " " + persona.getApellido());
                } else {
                    System.out.println("○ Persona ya existe: " + persona.getNombre() + " " + persona.getApellido());
                }
            } catch (Exception e) {
                System.err.println("Error al crear persona " + persona.getNombre() +
                        " " + persona.getApellido() + ": " + e.getMessage());
            }
        }

        System.out.println("\n💡 TIP: Para hacer reservas, usa la interfaz o el GestorReserva");
        System.out.println("   Para check-in, primero da de alta el huésped desde 'Alta Huésped'");
    }

    /**
     * Muestra un resumen de los datos creados
     */
    public void mostrarResumen() {
        System.out.println("\n=== RESUMEN DE HABITACIONES ===");

        List<TipoHabitacion> tipos = tipoHabitacionDAO.listarTodos();

        for (TipoHabitacion tipo : tipos) {
            Long cantidad = habitacionDAO.contarPorTipo(tipo);
            System.out.printf("%-30s: %2d habitaciones - $%.2f/noche (Cap: %d)%n",
                    tipo.getDescripcion(),
                    cantidad,
                    tipo.getCostoNoche(),
                    tipo.getCapacidad());
        }

        System.out.println("\n--- Estadísticas Generales ---");
        System.out.println("Total tipos de habitación: " + tipos.size());
        System.out.println("Total de habitaciones: " + habitacionDAO.listarTodas().size());
        System.out.println("Total de personas: " + personaDAO.obtenerTodas().size());
        System.out.println("Total de reservas: " + reservaDAO.obtenerTodas().size());
        System.out.println("Total de estadías: " + estadiaDAO.listarTodas().size());

        System.out.println("\n--- Habitaciones por Estado ---");
        for (EstadoHab estado : EstadoHab.values()) {
            long count = habitacionDAO.buscarPorEstado(estado).size();
            System.out.println("  " + estado.name() + ": " + count);
        }

        System.out.println("\n--- Estados en Catálogo ---");
        List<TipoEstado> estados = tipoEstadoDAO.listarTodos();
        for (TipoEstado estado : estados) {
            System.out.println("  • " + estado.getEstado().name());
        }
    }

    /**
     * Clase auxiliar para configuración de tipos de habitación
     */
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

    /**
     * Método main para ejecutar la inicialización
     */
    public static void main(String[] args) {
        System.out.println("🏨 HOTEL PREMIER - Inicializador de Datos");
        System.out.println("==========================================\n");

        InicializadorDatos inicializador = new InicializadorDatos();
        inicializador.inicializar();

        System.out.println("==========================================");
        System.out.println("✅ Sistema listo para usar");
        System.out.println("==========================================");
    }
}