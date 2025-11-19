package controllers.BuscarHuesped;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ResourceBundle;

public class BuscarHuespedController2 implements Initializable {

    @FXML
    private VBox containerHuespedes;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnSiguiente;

    private Huesped huespedSeleccionado;
    private ObservableList<Huesped> listaHuespedes;
    private GridPane filaSeleccionada;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarDatosPrueba();
        mostrarHuespedes();
        configurarBotones();
    }

    private void cargarDatosPrueba() {
        listaHuespedes = FXCollections.observableArrayList();

        // Datos de prueba - reemplazar con datos de BD
        listaHuespedes.add(new Huesped("Juan", "Pérez", "DNI", "12345678", "juan.perez@email.com"));
        listaHuespedes.add(new Huesped("María", "García", "DNI", "87654321", "maria.garcia@email.com"));
        listaHuespedes.add(new Huesped("Carlos", "López", "Pasaporte", "AB123456", "carlos.lopez@email.com"));
        listaHuespedes.add(new Huesped("Ana", "Martínez", "DNI", "45678901", "ana.martinez@email.com"));
        listaHuespedes.add(new Huesped("Roberto", "Sánchez", "Cédula", "1234567890", "roberto.sanchez@email.com"));
        listaHuespedes.add(new Huesped("Laura", "Rodríguez", "DNI", "56789012", "laura.rodriguez@email.com"));
        listaHuespedes.add(new Huesped("Diego", "Fernández", "Pasaporte", "CD789012", "diego.fernandez@email.com"));
        listaHuespedes.add(new Huesped("Sofía", "Moreno", "DNI", "67890123", "sofia.moreno@email.com"));
        listaHuespedes.add(new Huesped("Pedro", "Jiménez", "DNI", "78901234", "pedro.jimenez@email.com"));
        listaHuespedes.add(new Huesped("Elena", "Ortiz", "Pasaporte", "EF345678", "elena.ortiz@email.com"));
    }

    private void mostrarHuespedes() {
        for (Huesped huesped : listaHuespedes) {
            GridPane filaHuesped = crearFilaHuesped(huesped);
            containerHuespedes.getChildren().add(filaHuesped);
        }
    }

    private GridPane crearFilaHuesped(Huesped huesped) {
        GridPane gridRow = new GridPane();
        gridRow.setPrefHeight(50.0);
        gridRow.setMinHeight(50.0);
        gridRow.setMaxHeight(50.0);

        // Configurar las mismas columnas que el header
        gridRow.getColumnConstraints().addAll(
                crearColumnConstraint(150.0),
                crearColumnConstraint(150.0),
                crearColumnConstraint(150.0),
                crearColumnConstraint(150.0),
                crearColumnConstraint(250.0)
        );

        // Estilo inicial de la fila
        gridRow.setStyle("-fx-background-color: #fdfaf2; -fx-border-color: #e8dcc4; -fx-border-width: 0 0 1 0;");

        // Crear y agregar labels
        Label lblNombre = crearLabel(huesped.getNombre(), 0);
        Label lblApellido = crearLabel(huesped.getApellido(), 1);
        Label lblTipoDoc = crearLabel(huesped.getTipoDocumento(), 2);
        Label lblNumDoc = crearLabel(huesped.getNumeroDocumento(), 3);
        Label lblEmail = crearLabel(huesped.getEmail(), 4);

        gridRow.getChildren().addAll(lblNombre, lblApellido, lblTipoDoc, lblNumDoc, lblEmail);

        // Manejo de selección
        gridRow.setOnMouseClicked(e -> seleccionarFila(gridRow, huesped));

        // Efecto hover
        gridRow.setOnMouseEntered(e -> {
            if (filaSeleccionada != gridRow) {
                gridRow.setStyle("-fx-background-color: #f5ecd4; -fx-border-color: #e0cfa8; -fx-border-width: 0 0 1 0;");
            }
        });

        gridRow.setOnMouseExited(e -> {
            if (filaSeleccionada != gridRow) {
                gridRow.setStyle("-fx-background-color: #fdfaf2; -fx-border-color: #e8dcc4; -fx-border-width: 0 0 1 0;");
            }
        });

        gridRow.setCursor(Cursor.HAND);

        return gridRow;
    }

    private javafx.scene.layout.ColumnConstraints crearColumnConstraint(double prefWidth) {
        javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
        col.setPrefWidth(prefWidth);
        col.setMinWidth(prefWidth);
        col.setMaxWidth(prefWidth);
        return col;
    }

    private void seleccionarFila(GridPane fila, Huesped huesped) {
        // Deseleccionar la fila anterior
        if (filaSeleccionada != null) {
            filaSeleccionada.setStyle("-fx-background-color: #fdfaf2; -fx-border-color: #e8dcc4; -fx-border-width: 0 0 1 0;");
            filaSeleccionada.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: #2E1D0E;");
                }
            });
        }

        // Seleccionar la nueva fila
        filaSeleccionada = fila;
        huespedSeleccionado = huesped;

        fila.setStyle("-fx-background-color: linear-gradient(to right, #e7c375, #f5d68a); -fx-border-color: #c89b3c; -fx-border-width: 0 0 2 0;");
        fila.getChildren().forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: #2E1D0E; -fx-font-weight: bold;");
            }
        });

        System.out.println("Huésped seleccionado: " + huesped.getNombre() + " " + huesped.getApellido());
    }

    private Label crearLabel(String texto, int columnIndex) {
        Label label = new Label(texto);
        label.setFont(Font.font("Lucida Bright", 14.0));
        label.setStyle("-fx-text-fill: #2E1D0E;");
        label.setPadding(new Insets(10, 10, 10, 10));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinWidth(100.0);

        GridPane.setColumnIndex(label, columnIndex);

        return label;
    }

    private void configurarBotones() {
        btnCancelar.setOnAction(e -> {
            System.out.println("Cancelar presionado");
            // Lógica para cancelar
            limpiarSeleccion();
        });

        btnSiguiente.setOnAction(e -> {
            if (huespedSeleccionado != null) {
                System.out.println("Siguiente presionado con huésped: " +
                        huespedSeleccionado.getNombre() + " " +
                        huespedSeleccionado.getApellido());
                // Lógica para continuar al siguiente paso
            } else {
                System.out.println("No hay huésped seleccionado");
                // Mostrar mensaje de error o advertencia
            }
        });
    }

    private void limpiarSeleccion() {
        if (filaSeleccionada != null) {
            filaSeleccionada.setStyle("-fx-background-color: #fdfaf2; -fx-border-color: #e8dcc4; -fx-border-width: 0 0 1 0;");
            filaSeleccionada.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: #2E1D0E;");
                }
            });
            filaSeleccionada = null;
        }
        huespedSeleccionado = null;
    }

    // Métodos públicos para acceder al huésped seleccionado
    public Huesped getHuespedSeleccionado() {
        return huespedSeleccionado;
    }

    public boolean hayHuespedSeleccionado() {
        return huespedSeleccionado != null;
    }

    // Método para cargar huéspedes desde BD
    public void cargarHuespedesDesdeDB(ObservableList<Huesped> huespedes) {
        listaHuespedes = huespedes;
        containerHuespedes.getChildren().clear();
        containerHuespedes.getChildren().add(containerHuespedes.getChildren().get(0)); // Mantener el header
        mostrarHuespedes();
    }

    // ===== CLASE INTERNA HUESPED =====
    // Esta clase debería ser reemplazada por tu entidad real de BD
    public static class Huesped {
        private String nombre;
        private String apellido;
        private String tipoDocumento;
        private String numeroDocumento;
        private String email;

        public Huesped(String nombre, String apellido, String tipoDocumento, String numeroDocumento, String email) {
            this.nombre = nombre;
            this.apellido = apellido;
            this.tipoDocumento = tipoDocumento;
            this.numeroDocumento = numeroDocumento;
            this.email = email;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getApellido() {
            return apellido;
        }

        public void setApellido(String apellido) {
            this.apellido = apellido;
        }

        public String getTipoDocumento() {
            return tipoDocumento;
        }

        public void setTipoDocumento(String tipoDocumento) {
            this.tipoDocumento = tipoDocumento;
        }

        public String getNumeroDocumento() {
            return numeroDocumento;
        }

        public void setNumeroDocumento(String numeroDocumento) {
            this.numeroDocumento = numeroDocumento;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}