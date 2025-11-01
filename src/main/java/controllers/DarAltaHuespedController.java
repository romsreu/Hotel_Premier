package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;


public class DarAltaHuespedController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtOcupacion;
    @FXML private TextField txtCuit;
    @FXML private ComboBox<String> combPosicionIva;
    @FXML private TextField txtNumDoc;
    @FXML private TextField txtLocalidad;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtDepto;
    @FXML private TextField txtPiso;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtProvincia;
    @FXML private DatePicker dateNacimiento;
    @FXML private ComboBox<String> comboNacionalidad;
    @FXML private ComboBox<String> comboTipoDoc;
    @FXML private ComboBox<String> comboPais;
    @FXML private Button btnCancelar;
    @FXML private Button btnSiguiente;

    @FXML private ImageView imgFondo;
    @FXML private Pane vinietaFondo;


    @FXML
    public void initialize() {

        //Efectos por código, porque no soporta blur() en el inspector (fail)

        imgFondo.setEffect(null);
        GaussianBlur blur = new GaussianBlur(20);

        InnerShadow inner = new InnerShadow();
        inner.setColor(Color.rgb(0, 0, 0, 0.8)); // negro 80% opacidad
        inner.setRadius(250);
        inner.setChoke(0.30);

        inner.setInput(blur);
        imgFondo.setEffect(inner);

        RadialGradient radial = new RadialGradient(
                0, 0,
                0.5, 0.5,
                1.2, true,
                CycleMethod.NO_CYCLE,
                new Stop(0.5, Color.rgb(0, 0, 0, 0.35)),
                new Stop(0.8, Color.rgb(0, 0, 0, 0.6))
        );
        vinietaFondo.setBackground(
                new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundFill(radial, null, null)
                )
        );


        comboNacionalidad.getItems().addAll(
                "Argentina", "Uruguaya", "Chilena", "Paraguaya", "Brasileña", "Otra"
        );

        comboTipoDoc.getItems().addAll(
                "DNI", "Pasaporte", "Cédula", "Otro"
        );

        comboPais.getItems().addAll(
                "Argentina", "Uruguay", "Chile", "Paraguay", "Brasil", "Bolivia", "Perú"
        );

        comboPais.setValue("Argentina");
        comboTipoDoc.setValue("DNI");
    }

    @FXML
    private void onSiguienteClicked() {
        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
        String email = txtEmail.getText();
        String doc = txtNumDoc.getText();

        System.out.println(" --- DATOS DEL HUÉSPED ---");
        System.out.println("Nombre: " + nombre);
        System.out.println("Apellido: " + apellido);
        System.out.println("Email: " + email);
        System.out.println("Documento: " + doc);
        System.out.println("País: " + comboPais.getValue());
        System.out.println("Fecha de nacimiento: " + dateNacimiento.getValue());
        System.out.println("-----------------------------");

        // (más adelante acá se puede validar o guardar en BD)
    }
}

