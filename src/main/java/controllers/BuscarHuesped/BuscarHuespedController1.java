package controllers.BuscarHuesped;

import ar.utn.hotel.HotelPremier;
import enums.TipoDocumento;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import static utils.TextManager.*;

public class BuscarHuespedController1 {
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private ComboBox<TipoDocumento> cbTipoDocumento;
    @FXML private TextField txtNumeroDocumento;


    public void initialize() {
        aplicarMayusculas(txtNombre, txtApellido);
        soloLetras(txtNombre, txtApellido);
        limitarCaracteres(15, txtNombre, txtApellido);
        aplicarMascaraDNI(txtNumeroDocumento);
        cbTipoDocumento.getItems().addAll(TipoDocumento.values());
        cbTipoDocumento.setValue(TipoDocumento.DNI);
    }


    public void onCancelarClicked(ActionEvent actionEvent) {
        HotelPremier.cambiarA("menu");
    }

    public void onBuscarClicked(ActionEvent actionEvent) {
    }
}