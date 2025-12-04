package controllers.EstadoHabitaciones;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import ar.utn.hotel.dto.HabitacionReservaDTO;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.RegistroEstadoHabitacion;
import controllers.PopUp.PopUpController;
import enums.ContextoEstadoHabitaciones;
import enums.EstadoHabitacion;
import enums.PopUpType;
import enums.TipoHabitacion;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Cursor;
import utils.DataTransfer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class EstadoHabitacionesController2 {

    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    @FXML private Button btnConfirmar;
    @FXML private TabPane tabPane;
    @FXML private Label lbFechaDesde;
    @FXML private Label lbFechaHasta;
    @FXML private Label lblTitulo; // Opcional

    // Referencias a los GridPanes de cada tab
    private GridPane gridTodasHabitaciones;
    private GridPane gridIndividualEstandar;
    private GridPane gridDobleEstandar;
    private GridPane gridDobleSuperior;
    private GridPane gridSuperiorFamily;
    private GridPane gridSuiteDoble;

    // Referencias a los ScrollPanes
    private ScrollPane scrollTodasHabitaciones;
    private ScrollPane scrollIndividualEstandar;
    private ScrollPane scrollDobleEstandar;
    private ScrollPane scrollDobleSuperior;
    private ScrollPane scrollSuperiorFamily;
    private ScrollPane scrollSuiteDoble;

    // Configuración de visualización
    private static final double ANCHO_CELDA = 140.0;
    private static final double ALTO_CELDA = 70.0;
    private static final int COLUMNAS_VISIBLES = 6;
    private static final int FILAS_VISIBLES = 6;

    // DAO y datos
    private HabitacionDAOImpl habitacionDAO;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private ContextoEstadoHabitaciones contexto;
    private List<Habitacion> todasLasHabitaciones;

    // Para selección múltiple (solo en contextos RESERVAR y OCUPAR)
    private Set<CeldaSeleccionada> celdasSeleccionadas;

    @FXML
    public void initialize() {
        EstadoHabitacionDAO estadoHabitacionDAO = new EstadoHabitacionDAOImpl();
        habitacionDAO = new HabitacionDAOImpl(estadoHabitacionDAO);
        celdasSeleccionadas = new HashSet<>();

        // Obtener datos del DataTransfer
        fechaInicio = DataTransfer.getFechaDesdeEstadoHabitaciones();
        fechaFin = DataTransfer.getFechaHastaEstadoHabitaciones();
        contexto = DataTransfer.getContextoEstadoHabitaciones();

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lbFechaDesde.setText(fechaInicio.format(formato));
        lbFechaHasta.setText(fechaFin.format(formato));

        // Validar que se recibieron los datos
        if (fechaInicio == null || fechaFin == null || contexto == null) {
            mostrarError("Error: No se recibieron los datos correctamente");
            return;
        }

        configurarSegunContexto();
        inicializarTabs();
        cargarDatosReales();
    }

    private void configurarSegunContexto() {
        switch (contexto) {
            case MOSTRAR:
                if (lblTitulo != null) {
                    lblTitulo.setText("Estado de Habitaciones");
                }
                btnConfirmar.setVisible(false);
                btnConfirmar.setManaged(false);
                break;

            case RESERVAR:
                if (lblTitulo != null) {
                    lblTitulo.setText("Seleccionar Habitaciones para Reservar");
                }
                btnConfirmar.setText("Confirmar Reserva");
                btnConfirmar.setVisible(true);
                btnConfirmar.setManaged(true);
                break;

            case OCUPAR:
                if (lblTitulo != null) {
                    lblTitulo.setText("Seleccionar Habitación para Ocupar");
                }
                btnConfirmar.setText("Confirmar Ocupación");
                btnConfirmar.setVisible(true);
                btnConfirmar.setManaged(true);
                break;
        }
    }

    private void inicializarTabs() {
        // Obtener los tabs
        Tab tabTodas = tabPane.getTabs().get(0);
        Tab tabIndividualEstandar = tabPane.getTabs().get(1);
        Tab tabDobleEstandar = tabPane.getTabs().get(2);
        Tab tabDobleSuperior = tabPane.getTabs().get(3);
        Tab tabSuperiorFamily = tabPane.getTabs().get(4);
        Tab tabSuiteDoble = tabPane.getTabs().get(5);

        // Crear ScrollPanes y GridPanes para cada tab
        scrollTodasHabitaciones = crearScrollPane();
        gridTodasHabitaciones = crearGridPane();
        scrollTodasHabitaciones.setContent(gridTodasHabitaciones);
        tabTodas.setContent(scrollTodasHabitaciones);

        scrollIndividualEstandar = crearScrollPane();
        gridIndividualEstandar = crearGridPane();
        scrollIndividualEstandar.setContent(gridIndividualEstandar);
        tabIndividualEstandar.setContent(scrollIndividualEstandar);

        scrollDobleEstandar = crearScrollPane();
        gridDobleEstandar = crearGridPane();
        scrollDobleEstandar.setContent(gridDobleEstandar);
        tabDobleEstandar.setContent(scrollDobleEstandar);

        scrollDobleSuperior = crearScrollPane();
        gridDobleSuperior = crearGridPane();
        scrollDobleSuperior.setContent(gridDobleSuperior);
        tabDobleSuperior.setContent(scrollDobleSuperior);

        scrollSuperiorFamily = crearScrollPane();
        gridSuperiorFamily = crearGridPane();
        scrollSuperiorFamily.setContent(gridSuperiorFamily);
        tabSuperiorFamily.setContent(scrollSuperiorFamily);

        scrollSuiteDoble = crearScrollPane();
        gridSuiteDoble = crearGridPane();
        scrollSuiteDoble.setContent(gridSuiteDoble);
        tabSuiteDoble.setContent(scrollSuiteDoble);
    }

    private ScrollPane crearScrollPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scroll.setPrefViewportWidth(ANCHO_CELDA * COLUMNAS_VISIBLES);
        scroll.setPrefViewportHeight(ALTO_CELDA * FILAS_VISIBLES);

        configurarScrollDiscreto(scroll);

        return scroll;
    }

    private void configurarScrollDiscreto(ScrollPane scroll) {
        scroll.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            double contentWidth = scroll.getContent().getBoundsInLocal().getWidth();
            double viewportWidth = scroll.getViewportBounds().getWidth();
            double maxScroll = contentWidth - viewportWidth;

            if (maxScroll > 0) {
                double pixelPosition = newVal.doubleValue() * maxScroll;
                double snappedPosition = Math.round(pixelPosition / ANCHO_CELDA) * ANCHO_CELDA;
                double snappedValue = snappedPosition / maxScroll;

                if (Math.abs(newVal.doubleValue() - snappedValue) > 0.001) {
                    scroll.setHvalue(snappedValue);
                }
            }
        });

        scroll.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            double contentHeight = scroll.getContent().getBoundsInLocal().getHeight();
            double viewportHeight = scroll.getViewportBounds().getHeight();
            double maxScroll = contentHeight - viewportHeight;

            if (maxScroll > 0) {
                double pixelPosition = newVal.doubleValue() * maxScroll;
                double snappedPosition = Math.round(pixelPosition / ALTO_CELDA) * ALTO_CELDA;
                double snappedValue = snappedPosition / maxScroll;

                if (Math.abs(newVal.doubleValue() - snappedValue) > 0.001) {
                    scroll.setVvalue(snappedValue);
                }
            }
        });
    }

    private GridPane crearGridPane() {
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);
        grid.setSnapToPixel(true);
        return grid;
    }

    private void cargarDatosReales() {
        // Cargar todas las habitaciones desde la BD
        todasLasHabitaciones = habitacionDAO.listarTodas();

        if (todasLasHabitaciones.isEmpty()) {
            mostrarError("No hay habitaciones registradas en el sistema");
            return;
        }

        // Cargar "Todas las Habitaciones"
        cargarGrilla(gridTodasHabitaciones, fechaInicio, fechaFin, todasLasHabitaciones);

        // Cargar cada tab específico por tipo
        cargarGrillaPorTipo(gridIndividualEstandar, TipoHabitacion.INDIVIDUAL_ESTANDAR);
        cargarGrillaPorTipo(gridDobleEstandar, TipoHabitacion.DOBLE_ESTANDAR);
        cargarGrillaPorTipo(gridDobleSuperior, TipoHabitacion.DOBLE_SUPERIOR);
        cargarGrillaPorTipo(gridSuperiorFamily, TipoHabitacion.SUPERIOR_FAMILY_PLAN);
        cargarGrillaPorTipo(gridSuiteDoble, TipoHabitacion.SUITE_DOBLE);
    }

    private void cargarGrillaPorTipo(GridPane grid, enums.TipoHabitacion tipo) {
        List<Habitacion> habitacionesTipo = todasLasHabitaciones.stream()
                .filter(h -> h.getTipo() != null &&
                        h.getTipo().getNombre().equals(tipo.name()))
                .collect(Collectors.toList());

        cargarGrilla(grid, fechaInicio, fechaFin, habitacionesTipo);
    }

    private void cargarGrilla(GridPane grid, LocalDate inicio, LocalDate fin,
                              List<Habitacion> habitaciones) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        if (habitaciones.isEmpty()) {
            Label lblVacio = new Label("No hay habitaciones de este tipo");
            lblVacio.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            grid.add(lblVacio, 0, 0);
            return;
        }

        long cantidadDias = ChronoUnit.DAYS.between(inicio, fin) + 1;
        int numFilas = (int) cantidadDias + 1; // +1 para encabezado
        int numColumnas = habitaciones.size() + 1; // +1 para columna de fechas

        // Configurar constraints de columnas
        for (int i = 0; i < numColumnas; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(ANCHO_CELDA);
            col.setPrefWidth(ANCHO_CELDA);
            col.setMaxWidth(ANCHO_CELDA);
            col.setHgrow(Priority.NEVER);
            grid.getColumnConstraints().add(col);
        }

        // Configurar constraints de filas
        for (int i = 0; i < numFilas; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(ALTO_CELDA);
            row.setPrefHeight(ALTO_CELDA);
            row.setMaxHeight(ALTO_CELDA);
            row.setVgrow(Priority.NEVER);
            grid.getRowConstraints().add(row);
        }

        // Celda vacía superior izquierda
        StackPane esquina = crearCeldaEncabezado("Fecha");
        grid.add(esquina, 0, 0);

        // Encabezados de columnas (habitaciones)
        for (int col = 0; col < habitaciones.size(); col++) {
            Habitacion hab = habitaciones.get(col);
            String texto = hab.getTipo().getNombre() + " #" + hab.getNumero();
            StackPane header = crearCeldaEncabezado(texto);
            grid.add(header, col + 1, 0);
        }

        // Contenido de la grilla
        LocalDate fechaActual = inicio;

        for (int fila = 0; fila < cantidadDias; fila++) {
            // Encabezado de fila (fecha)
            StackPane lblFecha = crearCeldaEncabezado(
                    fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            grid.add(lblFecha, 0, fila + 1);

            // Celdas de datos
            for (int col = 0; col < habitaciones.size(); col++) {
                Habitacion hab = habitaciones.get(col);
                EstadoHabitacion estado = obtenerEstadoHabitacion(hab, fechaActual);
                StackPane celda = crearCeldaEstado(fechaActual, hab, estado);
                grid.add(celda, col + 1, fila + 1);
            }

            fechaActual = fechaActual.plusDays(1);
        }
    }

    private enums.EstadoHabitacion obtenerEstadoHabitacion(Habitacion habitacion, LocalDate fecha) {
        RegistroEstadoHabitacion registro = habitacion.getEstadoEn(fecha);

        if (registro != null && registro.getEstado() != null) {
            return registro.getEstado().getEstado();
        }

        // Por defecto, si no hay registro, asumir disponible
        return enums.EstadoHabitacion.DISPONIBLE;
    }

    private StackPane crearCeldaEncabezado(String texto) {
        StackPane celda = new StackPane();
        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        celda.setStyle("-fx-background-color: #2d3748; -fx-border-color: #1a202c; -fx-border-width: 1;");

        Label label = new Label(texto);
        label.setStyle("-fx-text-fill: #f7fafc; -fx-font-weight: bold; -fx-font-size: 12px;");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setMaxWidth(ANCHO_CELDA - 10);

        celda.getChildren().add(label);
        return celda;
    }

    private StackPane crearCeldaEstado(LocalDate fecha, Habitacion habitacion, enums.EstadoHabitacion estado) {
        StackPane celda = new StackPane();
        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Color según estado
        String colorFondo = switch (estado) {
            case DISPONIBLE -> "#48bb78"; // Verde
            case RESERVADA -> "#f56565";  // Rojo
            case MANTENIMIENTO -> "#4299e1"; // Azul
            case OCUPADA -> "#ed8936"; // Naranja
        };

        celda.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 1;",
                colorFondo
        ));

        Label label = new Label(estado.name()); // Cambiado de toString() a name()
        label.setStyle("-fx-text-fill: #1a202c; -fx-font-weight: bold; -fx-font-size: 13px;");
        label.setAlignment(Pos.CENTER);

        celda.getChildren().add(label);

        // Configurar interactividad según contexto
        if (contexto != ContextoEstadoHabitaciones.MOSTRAR) {
            // Solo permitir selección si está disponible
            if (estado == enums.EstadoHabitacion.DISPONIBLE) {
                celda.setCursor(Cursor.HAND);
                celda.setOnMouseClicked(e -> handleCeldaClickSeleccion(celda, fecha, habitacion, estado));
                celda.setOnMouseEntered(e -> celda.setOpacity(0.8));
                celda.setOnMouseExited(e -> celda.setOpacity(1.0));
            }
        } else {
            // Solo modo visualización
            celda.setCursor(Cursor.HAND);
            celda.setOnMouseClicked(e -> handleCeldaClickInfo(fecha, habitacion, estado));
            celda.setOnMouseEntered(e -> celda.setOpacity(0.8));
            celda.setOnMouseExited(e -> celda.setOpacity(1.0));
        }

        return celda;
    }

    private void handleCeldaClickInfo(LocalDate fecha, Habitacion habitacion, enums.EstadoHabitacion estado) {
        String mensaje = String.format(
                "%s #%d\n\n" +
                        "Fecha: %s\n" +
                        "Estado: %s\n" +
                        "Capacidad: %d personas\n" +
                        "Costo por noche: $%.2f",
                habitacion.getTipo().getDescripcion(),
                habitacion.getNumero(),
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                estado.name(),
                habitacion.getTipo().getCapacidad(),
                habitacion.getTipo().getCostoNoche()
        );

        mostrarInfo(mensaje);
    }

    private void handleCeldaClickSeleccion(StackPane celda, LocalDate fecha,
                                           Habitacion habitacion, enums.EstadoHabitacion estado) {
        CeldaSeleccionada celdaSel = new CeldaSeleccionada(fecha, habitacion.getNumero());

        if (celdasSeleccionadas.contains(celdaSel)) {
            // Deseleccionar
            celdasSeleccionadas.remove(celdaSel);
            celda.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 1;",
                    "#48bb78" // Verde original
            ));
        } else {
            // Seleccionar
            if (contexto == ContextoEstadoHabitaciones.OCUPAR && !celdasSeleccionadas.isEmpty()) {
                mostrarAdvertencia("Solo puede seleccionar una habitación para ocupar");
                return;
            }

            celdasSeleccionadas.add(celdaSel);
            celda.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 3;",
                    "#38a169" // Verde más oscuro para selección
            ));
        }

        System.out.println("Celdas seleccionadas: " + celdasSeleccionadas.size());
    }

    @FXML
    private void onCancelarClicked() {
        DataTransfer.limpiar();
        HotelPremier.cambiarA("menu");
    }

    @FXML
    private void onVovlerClicked() {
        HotelPremier.cambiarA("estado_habs1");
    }

    @FXML
    private void onConfirmarClicked() {
        if (celdasSeleccionadas.isEmpty()) {
            mostrarAdvertencia("Debe seleccionar al menos una habitación");
            return;
        }

        switch (contexto) {
            case RESERVAR:
                confirmarReserva();
                break;
            case OCUPAR:
                confirmarOcupacion();
                break;
            default:
                break;
        }
    }

    private void confirmarReserva() {
        // 1. Agrupar las celdas seleccionadas por número de habitación
        Map<Integer, List<LocalDate>> habitacionPorFechas = new HashMap<>();

        for (CeldaSeleccionada celda : celdasSeleccionadas) {
            habitacionPorFechas.computeIfAbsent(celda.numeroHabitacion, k -> new ArrayList<>())
                    .add(celda.fecha);
        }

        // 2. Crear lista de DTOs
        List<HabitacionReservaDTO> habitacionesDTO = new ArrayList<>();

        for (Map.Entry<Integer, List<LocalDate>> entry : habitacionPorFechas.entrySet()) {
            Integer numeroHab = entry.getKey();
            List<LocalDate> fechas = entry.getValue();

            fechas.sort(LocalDate::compareTo);
            LocalDate fechaIngreso = fechas.get(0);
            LocalDate fechaEgreso = fechas.get(fechas.size() - 1);

            Habitacion habitacion = todasLasHabitaciones.stream()
                    .filter(h -> h.getNumero().equals(numeroHab))
                    .findFirst()
                    .orElse(null);

            if (habitacion != null) {
                long cantidadNoches = ChronoUnit.DAYS.between(fechaIngreso, fechaEgreso) + 1;
                double costoNoche = habitacion.getTipo().getCostoNoche();
                double costoTotal = cantidadNoches * costoNoche;

                HabitacionReservaDTO dto = HabitacionReservaDTO.builder()
                        .numeroHabitacion(numeroHab)
                        .tipoHabitacion(habitacion.getTipo().getDescripcion())
                        .costoNoche(costoNoche)
                        .piso(habitacion.getPiso())
                        .capacidad(habitacion.getTipo().getCapacidad())
                        .fechaIngreso(fechaIngreso)
                        .fechaEgreso(fechaEgreso)
                        .cantidadNoches((int) cantidadNoches)
                        .costoTotal(costoTotal)
                        .build();

                habitacionesDTO.add(dto);
            }
        }

        habitacionesDTO.sort(Comparator.comparing(HabitacionReservaDTO::getNumeroHabitacion));

        DataTransfer.setHabitacionesSeleccionadas(habitacionesDTO);
        HotelPremier.cambiarA("reservar_hab1");
    }

    private void confirmarOcupacion() {
        CeldaSeleccionada celda = celdasSeleccionadas.iterator().next();

        String mensaje = String.format(
                "¿Desea confirmar la ocupación?\n\nHabitación: %d\nFecha: %s",
                celda.numeroHabitacion,
                celda.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        PopUpController.mostrarPopUpConCallback(
                PopUpType.CONFIRMATION,
                mensaje,
                confirmado -> {
                    if (confirmado) {
                        // TODO: Aquí llamarías a tu lógica de negocio para ocupar
                        // Ejemplo: habitacionDAO.ocuparHabitaciones(...)

                        mostrarExito("Ocupación confirmada exitosamente");
                        HotelPremier.cambiarA("menu");
                    }
                }
        );
    }

    private void mostrarError(String mensaje) {
        PopUpController.mostrarPopUp(PopUpType.ERROR, mensaje);
    }

    private void mostrarAdvertencia(String mensaje) {
        PopUpController.mostrarPopUp(PopUpType.WARNING, mensaje);
    }

    private void mostrarExito(String mensaje) {
        PopUpController.mostrarPopUp(PopUpType.SUCCESS, mensaje);
    }

    private void mostrarInfo(String mensaje) {
        PopUpController.mostrarPopUp(PopUpType.INFO, mensaje);
    }

    // Clase auxiliar para rastrear celdas seleccionadas
    private static class CeldaSeleccionada {
        LocalDate fecha;
        Integer numeroHabitacion;

        CeldaSeleccionada(LocalDate fecha, Integer numeroHabitacion) {
            this.fecha = fecha;
            this.numeroHabitacion = numeroHabitacion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CeldaSeleccionada that = (CeldaSeleccionada) o;
            return Objects.equals(fecha, that.fecha) &&
                    Objects.equals(numeroHabitacion, that.numeroHabitacion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fecha, numeroHabitacion);
        }
    }
}