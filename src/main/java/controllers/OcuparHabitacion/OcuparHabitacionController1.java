package controllers.OcuparHabitacion;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.gestor.GestorHuesped;
import ar.utn.hotel.model.Huesped;
import controllers.PopUp.PopUpController;
import enums.PopUpType;
import enums.TipoDocumento;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DataTransfer;

import java.util.List;

import static utils.TextManager.*;

public class OcuparHabitacionController1 {

    @FXML private RadioButton rbHuesped;
    @FXML private RadioButton rbAcompanante;
    @FXML private ToggleGroup tgTipoPersona;
    @FXML private TextField tfNombre;
    @FXML private TextField tfApellido;
    @FXML private ComboBox<TipoDocumento> cbTipoDocumento;
    @FXML private TextField tfNumeroDocumento;
    @FXML private Button btnCancelar;
    @FXML private Button btnAceptar;

    private GestorHuesped gestorHuesped;

    @FXML
    public void initialize() {
        gestorHuesped = new GestorHuesped();

        // Verificar que haya habitaciones seleccionadas
        if (DataTransfer.getHabitacionesSeleccionadas() == null ||
                DataTransfer.getHabitacionesSeleccionadas().isEmpty()) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "No hay habitaciones seleccionadas"
            );
            HotelPremier.cambiarA("menu");
            return;
        }

        configurarCampos();
        configurarRadioButtons();
        cargarTiposDocumento();
    }

    private void configurarCampos() {
        aplicarMayusculas(tfNombre, tfApellido);
        soloLetras(tfNombre, tfApellido);
        soloNumeros(tfNumeroDocumento);
        limitarCaracteres(15, tfNombre, tfApellido);
        aplicarMascaraDNI(tfNumeroDocumento);
    }

    private void configurarRadioButtons() {
        rbHuesped.setSelected(true);
        habilitarCamposDNI(true);

        tgTipoPersona.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbHuesped) {
                habilitarCamposDNI(true);
            } else if (newVal == rbAcompanante) {
                habilitarCamposDNI(false);
                limpiarCamposDNI();
            }
        });
    }

    private void habilitarCamposDNI(boolean habilitar) {
        cbTipoDocumento.setDisable(!habilitar);
        tfNumeroDocumento.setDisable(!habilitar);
    }

    private void limpiarCamposDNI() {
        cbTipoDocumento.setValue(null);
        tfNumeroDocumento.clear();
    }

    private void cargarTiposDocumento() {
        cbTipoDocumento.getItems().addAll(TipoDocumento.values());
        cbTipoDocumento.setValue(TipoDocumento.DNI);
    }

    @FXML
    public void onCancelarClicked(ActionEvent actionEvent) {
        DataTransfer.limpiar();
        HotelPremier.cambiarA("menu");
    }

    @FXML
    public void onAceptarClicked(ActionEvent actionEvent) {
        String nombre = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty()) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Debe ingresar nombre y apellido"
            );
            return;
        }

        if (rbHuesped.isSelected()) {
            buscarHuespedes(nombre, apellido);
        } else if (rbAcompanante.isSelected()) {
            buscarAcompanantes(nombre, apellido);
        }
    }
    private void buscarAcompanantes(String nombre, String apellido) {
        try {
            // Los acompañantes no requieren DNI, solo nombre y apellido
            List<Huesped> personas = gestorHuesped.buscarPersonaPorNombreApellido(nombre, apellido);

            if (personas == null || personas.isEmpty()) {
                PopUpController.mostrarPopUp(
                        PopUpType.WARNING,
                        "No se encontraron personas con esos datos"
                );
                return;
            }

            // Guardar resultados y pasar a la siguiente pantalla
            DataTransfer.setHuespedesEnBusqueda(personas);
            HotelPremier.cambiarA("ocupar_hab2");

        } catch (Exception e) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "Error al buscar personas:\n" + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void buscarHuespedes(String nombre, String apellido) {
        String numeroDocumento = tfNumeroDocumento.getText().trim();

        try {
            List<Huesped> huespedes;

            if (!numeroDocumento.isEmpty()) {
                // Buscar por nombre, apellido Y DNI
                huespedes = gestorHuesped.buscarPorNombreApellidoDNI(nombre, apellido, numeroDocumento);
            } else {
                // Buscar solo por nombre y apellido
                huespedes = gestorHuesped.buscarPorNombreApellido(nombre, apellido);
            }

            if (huespedes == null || huespedes.isEmpty()) {
                PopUpController.mostrarPopUp(
                        PopUpType.WARNING,
                        "No se encontraron huéspedes con esos datos"
                );
                return;
            }

            // Guardar resultados y pasar a la siguiente pantalla
            DataTransfer.setHuespedesEnBusqueda(huespedes);
            HotelPremier.cambiarA("ocupar_hab2");

        } catch (Exception e) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "Error al buscar huéspedes:\n" + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}