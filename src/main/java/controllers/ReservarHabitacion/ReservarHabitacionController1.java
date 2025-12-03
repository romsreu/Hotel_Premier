package controllers.ReservarHabitacion;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.model.Habitacion;
import controllers.PopUp.PopUpController;
import enums.PopUpType;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import utils.DataTransfer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReservarHabitacionController1 {

    @FXML private GridPane gridPane;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    @FXML private Button btnAceptar;

    private HabitacionDAOImpl habitacionDAO;
    private Map<Integer, LocalDate> habitacionesSeleccionadas;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @FXML
    public void initialize() {
        habitacionDAO = new HabitacionDAOImpl(new EstadoHabitacionDAOImpl());

        // Obtener datos del DataTransfer
        //habitacionesSeleccionadas = DataTransfer.getHabitacionesSeleccionadas();
        fechaInicio = DataTransfer.getFechaDesdeEstadoHabitaciones();
        fechaFin = DataTransfer.getFechaHastaEstadoHabitaciones();

        if (habitacionesSeleccionadas == null || habitacionesSeleccionadas.isEmpty()) {
            PopUpController.mostrarPopUp(PopUpType.ERROR,
                    "No hay habitaciones seleccionadas");
            HotelPremier.cambiarA("menu");
            return;
        }

        cargarResumen();
    }

    private void cargarResumen() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Limpiar filas existentes (excepto la primera que son los encabezados)
        gridPane.getRowConstraints().clear();

        // Agregar constraint para la fila de encabezados
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(40.0);
        headerRow.setPrefHeight(40.0);
        gridPane.getRowConstraints().add(headerRow);

        int fila = 1; // Empezamos desde 1 porque 0 son los encabezados

        for (Map.Entry<Integer, LocalDate> entry : habitacionesSeleccionadas.entrySet()) {
            Integer numeroHabitacion = entry.getKey();
            LocalDate fechaSeleccionada = entry.getValue();

            // Buscar la habitación en la BD
            Habitacion habitacion = habitacionDAO.buscarPorNumero(numeroHabitacion);

            if (habitacion == null) {
                continue;
            }

            // Agregar constraint para esta fila
            RowConstraints row = new RowConstraints();
            row.setMinHeight(40.0);
            row.setPrefHeight(40.0);
            gridPane.getRowConstraints().add(row);

            // Columna 0: Número de habitación
            Label lblNumero = crearLabel(numeroHabitacion.toString());
            gridPane.add(lblNumero, 0, fila);

            // Columna 1: Tipo de habitación
            Label lblTipo = crearLabel(habitacion.getTipo().getDescripcion());
            gridPane.add(lblTipo, 1, fila);

            // Columna 2: Fecha de ingreso
            Label lblIngreso = crearLabel(fechaInicio.format(formato));
            gridPane.add(lblIngreso, 2, fila);

            // Columna 3: Fecha de egreso
            Label lblEgreso = crearLabel(fechaFin.format(formato));
            gridPane.add(lblEgreso, 3, fila);

            fila++;
        }
    }

    private Label crearLabel(String texto) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setFont(Font.font("Lucida Bright", 13));
        label.setStyle("-fx-padding: 10; -fx-text-fill: #2c3e50;");
        return label;
    }

    @FXML
    private void onCancelarClicked() {
        PopUpController.mostrarPopUpConCallback(
                PopUpType.CONFIRMATION,
                "¿Está seguro de cancelar la reserva?",
                confirmado -> {
                    if (confirmado) {
                        DataTransfer.limpiar();
                        HotelPremier.cambiarA("menu");
                    }
                }
        );
    }

    @FXML
    private void onVolverClicked() {
        // Volver a la pantalla de selección de habitaciones
        HotelPremier.cambiarA("estado_habs2");
    }

    @FXML
    private void onAceptarClicked() {
        // Ir a la siguiente pantalla (reservar_hab2)
        HotelPremier.cambiarA("reservar_hab2");
    }
}

