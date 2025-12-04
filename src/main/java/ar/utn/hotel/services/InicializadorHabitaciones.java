package ar.utn.hotel.services;

import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dao.ReservaDAO.RangoFechas;
import ar.utn.hotel.dao.TipoHabitacionDAO;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import ar.utn.hotel.dao.impl.PersonaDAOImpl;
import ar.utn.hotel.dao.impl.ReservaDAOImpl;
import ar.utn.hotel.dao.impl.TipoHabitacionDAOImpl;
import ar.utn.hotel.model.*;

import java.time.LocalDate;
import java.util.*;

public class InicializadorHabitaciones {

    private final HabitacionDAO habitacionDAO;
    private final TipoHabitacionDAO tipoHabitacionDAO;
    private final EstadoHabitacionDAO estadoHabitacionDAO;
    private final PersonaDAO personaDAO;
    private final ReservaDAO reservaDAO;

    // Configuración de tipos de habitación
    private static final List<TipoHabitacionConfig> TIPOS_CONFIG = Arrays.asList(
            new TipoHabitacionConfig("INDIVIDUAL_ESTANDAR", "Individual Estándar",
                    "Habitación individual con cama simple", 1, 80.0, 10),
            new TipoHabitacionConfig("DOBLE_ESTANDAR", "Doble Estándar",
                    "Habitación doble con dos camas individuales o una matrimonial", 2, 120.0, 18),
            new TipoHabitacionConfig("DOBLE_SUPERIOR", "Doble Superior",
                    "Habitación doble amplia con amenities premium", 2, 150.0, 8),
            new TipoHabitacionConfig("SUPERIOR_FAMILY_PLAN", "Superior Family Plan",
                    "Habitación familiar con espacio adicional", 4, 200.0, 10),
            new TipoHabitacionConfig("SUITE_DOBLE", "Suite Doble",
                    "Suite de lujo con sala de estar separada", 2, 300.0, 2)
    );

    public InicializadorHabitaciones() {
        this.estadoHabitacionDAO = new EstadoHabitacionDAOImpl();
        this.habitacionDAO = new HabitacionDAOImpl(estadoHabitacionDAO);
        this.tipoHabitacionDAO = new TipoHabitacionDAOImpl();
        this.personaDAO = new PersonaDAOImpl();
        this.reservaDAO = new ReservaDAOImpl(personaDAO, estadoHabitacionDAO);
    }

    public InicializadorHabitaciones(HabitacionDAO habitacionDAO,
                                     TipoHabitacionDAO tipoHabitacionDAO,
                                     EstadoHabitacionDAO estadoHabitacionDAO,
                                     PersonaDAO personaDAO,
                                     ReservaDAO reservaDAO) {
        this.habitacionDAO = habitacionDAO;
        this.tipoHabitacionDAO = tipoHabitacionDAO;
        this.estadoHabitacionDAO = estadoHabitacionDAO;
        this.personaDAO = personaDAO;
        this.reservaDAO = reservaDAO;
    }

    // Inicializa todas las habitaciones y tipos en la base de datos
    public void inicializar() {
        System.out.println("=== Iniciando carga de datos ===");

        // Primero inicializar el catálogo de estados
        inicializarCatalogoEstados();

        // Luego inicializar los tipos de habitación
        inicializarTiposHabitacion();

        // Finalmente inicializar las habitaciones
        inicializarHabitaciones();

        System.out.println("\n=== Carga de datos completada ===");
        mostrarResumen();
    }

    // Inicializa el catálogo de estados
    private void inicializarCatalogoEstados() {
        System.out.println("\n--- Inicializando catálogo de estados ---");

        for (enums.EstadoHabitacion estadoEnum : enums.EstadoHabitacion.values()) {
            if (!estadoHabitacionDAO.existeEstado(estadoEnum)) {
                EstadoHabitacion estado = EstadoHabitacion.builder()
                        .estado(estadoEnum)
                        .registros(new HashSet<>())
                        .build();

                estadoHabitacionDAO.guardar(estado);
                System.out.println("✓ Estado creado: " + estadoEnum.name());
            } else {
                System.out.println("○ Estado ya existe: " + estadoEnum.name());
            }
        }
    }

    // Inicializa los tipos de habitación
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

    // Inicializa las habitaciones
    private void inicializarHabitaciones() {
        System.out.println("\n--- Inicializando habitaciones ---");

        // Obtener el estado DISPONIBLE del catálogo
        EstadoHabitacion estadoDisponible = estadoHabitacionDAO.buscarPorEstado(
                enums.EstadoHabitacion.DISPONIBLE
        );

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
                            .historialEstados(new HashSet<>())
                            .reservas(new HashSet<>())
                            .build();

                    // Crear registro inicial con estado DISPONIBLE
                    RegistroEstadoHabitacion registroInicial = RegistroEstadoHabitacion.builder()
                            .habitacion(habitacion)
                            .estado(estadoDisponible)
                            .fechaDesde(LocalDate.now())
                            .fechaHasta(null)
                            .build();

                    // Agregar el registro al historial
                    habitacion.getHistorialEstados().add(registroInicial);

                    // Guardar la habitación (cascade persiste el registro)
                    habitacionDAO.guardar(habitacion);

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

    // Elimina todas las habitaciones y tipos, y vuelve a inicializar
    public void reinicializar() {
        System.out.println("\n=== Reinicializando base de datos ===");

        System.out.println("Eliminando todas las reservas...");
        for (Reserva reserva : reservaDAO.obtenerTodas()) {
            reservaDAO.eliminar(reserva.getId());
        }
        System.out.println("Reservas eliminadas");

        System.out.println("Eliminando todas las habitaciones...");
        for (Habitacion habitacion : habitacionDAO.listarTodas()) {
            habitacionDAO.eliminar(habitacion.getNumero());
        }
        System.out.println("Habitaciones eliminadas");

        System.out.println("Eliminando todos los tipos de habitación...");
        for (TipoHabitacion tipo : tipoHabitacionDAO.listarTodos()) {
            tipoHabitacionDAO.eliminar(tipo.getIdTipoHabitacion());
        }
        System.out.println("Tipos eliminados");

        inicializar();
    }

    // Muestra un resumen de las habitaciones cargadas
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

        System.out.println("\n--- Estadísticas ---");
        System.out.println("Total tipos de habitación: " + tipos.size());
        System.out.println("Total de habitaciones: " + habitacionDAO.listarTodas().size());
        System.out.println("Habitaciones disponibles: " + habitacionDAO.buscarDisponibles().size());

        System.out.println("\n--- Estados en catálogo ---");
        List<EstadoHabitacion> estados = estadoHabitacionDAO.listarTodos();
        for (EstadoHabitacion estado : estados) {
            System.out.println("  • " + estado.getEstado().name());
        }
    }

    // Método para probar creando reservas a nombre de JOSE RODRIGUEZ
    public void probarReserva() {
        System.out.println("\n=== PROBANDO SISTEMA DE RESERVAS ===");

        try {
            // 1. Buscar persona JOSE RODRIGUEZ
            Persona jose = personaDAO.buscarPorNombreApellido("JOSE", "RODRIGUEZ");

            if (jose == null) {
                System.out.println("✗ No se encontró la persona JOSE RODRIGUEZ");
                System.out.println("  Asegúrate de tener datos de prueba en la tabla 'persona'");
                return;
            }

            // 2. Buscar habitaciones disponibles
            System.out.println("\n--- Buscando habitaciones disponibles ---");
            List<Habitacion> disponibles = habitacionDAO.buscarDisponibles();

            if (disponibles.isEmpty()) {
                System.out.println("✗ No hay habitaciones disponibles");
                return;
            }

            System.out.println("✓ Encontradas " + disponibles.size() + " habitaciones disponibles");

            // 3. Preparar datos para reservar 2 habitaciones con fechas diferentes
            Map<Integer, RangoFechas> habitacionesConFechas = new HashMap<>();

            if (disponibles.size() >= 2) {
                Habitacion hab1 = disponibles.get(0);
                Habitacion hab2 = disponibles.get(1);

                // Habitación 1: mañana por 5 noches
                LocalDate fecha1Inicio = LocalDate.now().plusDays(1);
                LocalDate fecha1Fin = LocalDate.now().plusDays(5);
                habitacionesConFechas.put(hab1.getNumero(),
                        new RangoFechas(fecha1Inicio, fecha1Fin));

                System.out.println("\n--- Preparando reservas ---");
                System.out.println("Habitación " + hab1.getNumero() +
                        " (" + hab1.getTipo().getDescripcion() + "):");
                System.out.println("  Desde: " + fecha1Inicio);
                System.out.println("  Hasta: " + fecha1Fin);
                System.out.println("  Costo: $" + hab1.getCostoNoche() + "/noche");

                // Habitación 2: dentro de una semana por 6 noches (fechas diferentes!)
                LocalDate fecha2Inicio = LocalDate.now().plusDays(7);
                LocalDate fecha2Fin = LocalDate.now().plusDays(12);
                habitacionesConFechas.put(hab2.getNumero(),
                        new RangoFechas(fecha2Inicio, fecha2Fin));

                System.out.println("\nHabitación " + hab2.getNumero() +
                        " (" + hab2.getTipo().getDescripcion() + "):");
                System.out.println("  Desde: " + fecha2Inicio);
                System.out.println("  Hasta: " + fecha2Fin);
                System.out.println("  Costo: $" + hab2.getCostoNoche() + "/noche");
            } else {
                System.out.println("✗ No hay suficientes habitaciones disponibles para la prueba");
                return;
            }

            // 4. Crear las reservas
            System.out.println("\n--- Creando reservas en la base de datos ---");
            List<Reserva> reservas = reservaDAO.crearReservasPorNombreApellido(
                    "JOSE",
                    "RODRIGUEZ",
                    habitacionesConFechas
            );

            System.out.println("\n✓✓✓ RESERVAS CREADAS EXITOSAMENTE ✓✓✓");
            System.out.println("Total de reservas creadas: " + reservas.size());
            System.out.println("Cliente: " + jose.getNombre() + " " + jose.getApellido());

            System.out.println("\n--- Detalle de reservas creadas ---");
            for (Reserva reserva : reservas) {
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("Reserva ID: " + reserva.getId());
                System.out.println("Habitación: " + reserva.getHabitacion().getNumero() +
                        " - " + reserva.getHabitacion().getTipo().getDescripcion());
                System.out.println("Check-in: " + reserva.getFechaInicio());
                System.out.println("Check-out: " + reserva.getFechaFin());

                long noches = java.time.temporal.ChronoUnit.DAYS.between(
                        reserva.getFechaInicio(),
                        reserva.getFechaFin()
                ) + 1;
                double costoTotal = noches * reserva.getHabitacion().getCostoNoche();

                System.out.println("Noches: " + noches);
                System.out.println("Costo total: $" + costoTotal);

                // Verificar estado actual
                RegistroEstadoHabitacion estadoActual = reserva.getHabitacion().getEstadoActual();
                if (estadoActual != null) {
                    System.out.println("Estado actual: " + estadoActual.getEstado().getEstado().name());
                }
            }
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // 5. Mostrar estadísticas actualizadas
            System.out.println("\n--- Estadísticas actualizadas ---");
            System.out.println("Habitaciones disponibles: " + habitacionDAO.buscarDisponibles().size());
            System.out.println("Total de reservas en el sistema: " + reservaDAO.obtenerTodas().size());

            System.out.println("\n--- Verificación en BD ---");
            System.out.println("Puedes verificar en la base de datos:");
            System.out.println("  SELECT * FROM reserva;");
            System.out.println("  SELECT * FROM registro_estado_habitacion WHERE numero_habitacion IN (" +
                    reservas.get(0).getHabitacion().getNumero() + ", " +
                    reservas.get(1).getHabitacion().getNumero() + ");");

        } catch (Exception e) {
            System.err.println("\n✗✗✗ ERROR AL CREAR RESERVAS ✗✗✗");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Clase auxiliar para configuración
    private static class TipoHabitacionConfig {
        String nombre;
        String descripcion;
        Integer capacidad;
        Double costoNoche;
        int cantidad;

        TipoHabitacionConfig(String nombre, String displayName, String descripcion,
                             Integer capacidad, Double costoNoche, int cantidad) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.capacidad = capacidad;
            this.costoNoche = costoNoche;
            this.cantidad = cantidad;
        }
    }
}