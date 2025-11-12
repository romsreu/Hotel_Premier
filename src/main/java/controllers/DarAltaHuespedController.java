package controllers;

import enums.PopUpType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import static controllers.PopUpController.mostrarPopUp;
import static utils.TextManager.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DarAltaHuespedController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtOcupacion;
    @FXML private TextField txtCuit;
    @FXML private ComboBox<String> combPosicionIva;
    @FXML private TextField txtNumDoc;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumeroCalle;
    @FXML private TextField txtDepto;
    @FXML private TextField txtPiso;
    @FXML private TextField txtCodigoPostal;
    @FXML private DatePicker dateNacimiento;
    @FXML private ComboBox<String> comboNacionalidad;
    @FXML private ComboBox<String> comboLocalidad;
    @FXML private ComboBox<String> comboProvincia;
    @FXML private ComboBox<String> comboTipoDoc;
    @FXML private ComboBox<String> comboPais;
    @FXML private Button btnCancelar;
    @FXML private Button btnSiguiente;


    // === CLIENTE HTTP (se reutiliza para todas las llamadas) ===
    private final HttpClient http = HttpClient.newHttpClient();

    @FXML
    public void initialize() {

        aplicarMayusculas(txtNombre,txtApellido,txtCalle, txtOcupacion, txtEmail);
        aplicarMascaraDNI(txtNumDoc);
        aplicarMascaraCUIT(txtCuit);
        limitarCaracteres(15, txtNombre, txtApellido, txtCalle, txtOcupacion);
        limitarCaracteres(4, txtNumeroCalle, txtDepto, txtPiso, txtCodigoPostal);
        soloLetras(txtNombre, txtApellido, txtCalle, txtOcupacion);
        soloNumeros(txtNumeroCalle, txtDepto, txtPiso, txtCodigoPostal);
        cargarProvincias();


        comboProvincia.setOnAction(e -> {
            String prov = comboProvincia.getValue();
            if (prov != null && !prov.isBlank()) {
                cargarLocalidades(prov);
            }
        });

        comboNacionalidad.getItems().addAll("Argentina", "Uruguaya", "Chilena", "Paraguaya", "Brasileña", "Otra");
        comboTipoDoc.getItems().addAll("DNI", "Pasaporte", "Cédula", "Otro");
        comboTipoDoc.setValue("DNI");
        combPosicionIva.getItems().addAll("RESPONSABLE INSCRIPTO", "MONOTRIBUTISTA", "Otro");
        comboPais.getItems().addAll("Argentina");
        comboPais.setValue("Argentina");

    }

    // ===============================================================
    // CARGA DE PROVINCIAS DESDE API GEOREF
    // ===============================================================
    private void cargarProvincias() {
        comboProvincia.setDisable(true);
        comboProvincia.setPromptText("Cargando provincias...");

        String url = "https://apis.datos.gob.ar/georef/api/provincias?campos=nombre&max=100";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::procesarProvincias)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        comboProvincia.setPromptText("Error al cargar");
                        comboProvincia.setDisable(false);
                    });
                    return null;
                });
    }

    private void procesarProvincias(String json) {
        List<String> provincias = extraerNombres(json);
        Platform.runLater(() -> {
            comboProvincia.getItems().setAll(provincias);
            comboProvincia.setPromptText("Provincia");
            comboProvincia.setDisable(false);
        });
    }

    // ===============================================================
    // CARGA DE LOCALIDADES SEGÚN PROVINCIA
    // ===============================================================
    private void cargarLocalidades(String provinciaNombre) {
        comboLocalidad.setDisable(true);
        comboLocalidad.getItems().clear();
        comboLocalidad.setPromptText("Cargando localidades...");

        String provEncoded = URLEncoder.encode(provinciaNombre, StandardCharsets.UTF_8);
        String url = "https://apis.datos.gob.ar/georef/api/localidades?provincia=" + provEncoded
                + "&campos=nombre&max=5000";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    List<String> localidades = extraerNombres(json);
                    Platform.runLater(() -> {
                        comboLocalidad.getItems().setAll(localidades);
                        comboLocalidad.setPromptText("Localidad");
                        comboLocalidad.setDisable(false);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        comboLocalidad.setPromptText("Error al cargar");
                        comboLocalidad.setDisable(false);
                    });
                    return null;
                });
    }

    // ===============================================================
    // PARSER SIMPLE (sin dependencias externas)
    // ===============================================================
    private List<String> extraerNombres(String json) {
        List<String> nombres = new ArrayList<>();
        String[] partes = json.split("\"nombre\"\\s*:\\s*");
        for (int i = 1; i < partes.length; i++) {
            String resto = partes[i].trim();
            if (!resto.startsWith("\"")) continue;
            int fin = resto.indexOf('"', 1);
            if (fin > 1) {
                String nombre = resto.substring(1, fin).trim();
                if (!nombre.isBlank()) nombres.add(nombre);
            }
        }
        nombres.sort(String::compareToIgnoreCase);
        return nombres;
    }

    // ===============================================================
    // BOTÓN SIGUIENTE
    // ===============================================================
    @FXML
    private void onSiguienteClicked() {
        // Obtención de datos del formulario
        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
        String email = txtEmail.getText();
        String tipoDoc = comboTipoDoc.getValue();
        String doc = txtNumDoc.getText();
        String ocupacion = txtOcupacion.getText();
        String posicionIVA = combPosicionIva.getValue();
        String fechaNac = (dateNacimiento.getValue() != null) ? dateNacimiento.getValue().toString() : "No especificada";
        String nacionalidad = comboNacionalidad.getValue();
        String pais = comboPais.getValue();
        String provincia = comboProvincia.getValue();
        String localidad = comboLocalidad.getValue();
        String calle = txtCalle.getText();
        String nroCalle = txtNumeroCalle.getText();
        String depto = txtDepto.getText();
        String piso = txtPiso.getText();
        String codPostal = txtCodigoPostal.getText();
        String cuit = txtCuit.getText();
        mostrarPopUp(PopUpType.SUCCESS, "El email ingresado no es válido.");

        System.out.println("========================================");
        System.out.println("          DATOS DEL HUÉSPED");
        System.out.println("========================================");
        System.out.printf("%-22s%s%n", "Nombre:", nombre);
        System.out.printf("%-22s%s%n", "Apellido:", apellido);
        System.out.printf("%-22s%s%n", "Email:", email);
        System.out.printf("%-22s%s%n", "Tipo de Documento:", tipoDoc);
        System.out.printf("%-22s%s%n", "Número de Documento:", doc);
        System.out.printf("%-22s%s%n", "CUIT:", cuit);
        System.out.printf("%-22s%s%n", "Ocupación:", ocupacion);
        System.out.printf("%-22s%s%n", "Posición frente al IVA:", posicionIVA);
        System.out.printf("%-22s%s%n", "Fecha de Nacimiento:", fechaNac);
        System.out.printf("%-22s%s%n", "Nacionalidad:", nacionalidad);
        System.out.printf("%-22s%s%n", "País:", pais);
        System.out.printf("%-22s%s%n", "Provincia:", provincia);
        System.out.printf("%-22s%s%n", "Localidad:", localidad);
        System.out.printf("%-22s%s%n", "Calle:", calle);
        System.out.printf("%-22s%s%n", "Número:", nroCalle);
        System.out.printf("%-22s%s%n", "Depto:", depto);
        System.out.printf("%-22s%s%n", "Piso:", piso);
        System.out.printf("%-22s%s%n", "Código Postal:", codPostal);
        System.out.println("----------------------------------------");
    }
}
