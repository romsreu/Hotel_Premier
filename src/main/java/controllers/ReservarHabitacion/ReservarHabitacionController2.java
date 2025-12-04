package controllers.ReservarHabitacion;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.PersonaDAOImpl;
import ar.utn.hotel.dao.impl.ReservaDAOImpl;
import ar.utn.hotel.dto.HabitacionReservaDTO;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.gestor.GestorReserva;
import ar.utn.hotel.model.Persona;
import controllers.PopUp.PopUpController;
import enums.PopUpType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import utils.DataTransfer;
import utils.Validator;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.TextManager.*;

public class ReservarHabitacionController2 {

    @FXML private TextField tfNombre;
    @FXML private TextField tfApellido;
    @FXML private TextField tfTelefono;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    @FXML private Button btnConfirmar;

    // Iconos de validación
    @FXML private ImageView nombreO;
    @FXML private ImageView apellidoO;
    @FXML private ImageView telefonoO;

    private Validator validator;
    private PersonaDAOImpl personaDAO;
    private GestorReserva gestorReserva;
    private List<HabitacionReservaDTO> habitacionesSeleccionadas;

    @FXML
    public void initialize() {
        // Inicializar DAOs y gestor
        personaDAO = new PersonaDAOImpl();
        EstadoHabitacionDAOImpl estadoDAO = new EstadoHabitacionDAOImpl();
        ReservaDAO reservaDAO = new ReservaDAOImpl(personaDAO, estadoDAO);
        gestorReserva = new GestorReserva(reservaDAO, personaDAO);

        // Obtener habitaciones seleccionadas
        habitacionesSeleccionadas = DataTransfer.getHabitacionesSeleccionadas();

        if (habitacionesSeleccionadas == null || habitacionesSeleccionadas.isEmpty()) {
            PopUpController.mostrarPopUp(PopUpType.ERROR,
                    "No hay habitaciones seleccionadas");
            HotelPremier.cambiarA("menu");
            return;
        }

        configurarFormatoDeTexto();
        configurarValidaciones();
        ocultarIconosValidacion();
    }

    // ==================== CONFIGURACIÓN INICIAL ====================

    private void configurarFormatoDeTexto() {
        aplicarMayusculas(tfNombre, tfApellido);
        aplicarFiltrosDeEntrada();
        limitarLongitudDeCampos();
    }

    private void limitarLongitudDeCampos() {
        limitarCaracteres(15, tfNombre, tfApellido, tfTelefono);
    }

    private void aplicarFiltrosDeEntrada() {
        soloLetras(tfNombre, tfApellido);
        soloNumeros(tfTelefono);
    }

    private void configurarValidaciones() {
        validator = new Validator();

        // Validar campos obligatorios
        validator.addRule(tfNombre, nombreO).required().minLength(2);
        validator.addRule(tfApellido, apellidoO).required().minLength(2);
        validator.addRule(tfTelefono, telefonoO).required().minLength(7);

    }

    private void ocultarIconosValidacion() {
        if (nombreO != null) nombreO.setVisible(false);
        if (apellidoO != null) apellidoO.setVisible(false);
        if (telefonoO != null) telefonoO.setVisible(false);
    }

    // ==================== EVENTOS DE BOTONES ====================

    @FXML
    public void onCancelarClicked(ActionEvent actionEvent) {
        PopUpController.mostrarPopUpConCallback(
                PopUpType.CONFIRMATION,
                "¿Está seguro de cancelar la reserva?\nSe perderán todos los datos.",
                confirmado -> {
                    if (confirmado) {
                        DataTransfer.limpiar();
                        HotelPremier.cambiarA("menu");
                    }
                }
        );
    }

    @FXML
    public void onVolverClicked(ActionEvent actionEvent) {
        HotelPremier.cambiarA("reservar_hab1");
    }

    @FXML
    public void onConfirmarClicked(ActionEvent actionEvent) {
        // Validar campos
        if (!validator.validateAll()) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Complete los campos obligatorios correctamente:\n\n" +
                            "• Nombre (mínimo 2 caracteres)\n" +
                            "• Apellido (mínimo 2 caracteres)\n" +
                            "• Teléfono (opcional, mínimo 7 dígitos)"
            );
            return;
        }

        String nombre = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        String telefono = tfTelefono.getText().trim();

        // Buscar si la persona existe
        verificarYConfirmarReserva(nombre, apellido, telefono);
    }

    // ==================== LÓGICA DE VERIFICACIÓN Y RESERVA ====================

    private void verificarYConfirmarReserva(String nombre, String apellido, String telefono) {
        try {
            // Buscar si la persona existe en BD
            Persona personaExistente = personaDAO.buscarPorNombreApellido(nombre, apellido);

            boolean existe = (personaExistente != null);

            // Construir mensaje de confirmación
            String mensaje = construirMensajeConfirmacion(nombre, apellido, telefono, existe);

            // Mostrar confirmación
            PopUpController.mostrarPopUpConCallback(
                    PopUpType.CONFIRMATION,
                    mensaje,
                    confirmado -> {
                        if (confirmado) {
                            procesarReserva(nombre, apellido, telefono, existe);
                        }
                    }
            );

        } catch (Exception e) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "Error al verificar la persona:\n" + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private String construirMensajeConfirmacion(String nombre, String apellido,
                                                String telefono, boolean existe) {
        double costoTotal = calcularCostoTotal();
        int totalNoches = calcularTotalNoches();

        String estadoPersona = existe ? "✓ PERSONA EXISTENTE" : "⚠ NUEVA PERSONA (se creará)";

        return String.format(
                "¿Confirmar reserva?\n\n" +
                        "═══════════════════════════════════\n" +
                        "%s\n" +
                        "Nombre: %s %s\n" +
                        "Teléfono: %s\n\n" +
                        "RESUMEN DE RESERVA:\n" +
                        "• Habitaciones: %d\n" +
                        "• Total noches: %d\n" +
                        "• Costo total: $%.2f\n" +
                        "═══════════════════════════════════",
                estadoPersona,
                nombre,
                apellido,
                telefono.isEmpty() ? "No especificado" : telefono,
                habitacionesSeleccionadas.size(),
                totalNoches,
                costoTotal
        );
    }

    private void procesarReserva(String nombre, String apellido, String telefono, boolean existe) {
        try {
            // Si la persona NO existe, crearla primero
            if (!existe) {
                crearPersona(nombre, apellido, telefono);
            }

            // Convertir habitaciones seleccionadas a Map<Integer, RangoFechas>
            Map<Integer, ReservaDAO.RangoFechas> habitacionesConFechas = new HashMap<>();

            for (HabitacionReservaDTO hab : habitacionesSeleccionadas) {
                habitacionesConFechas.put(
                        hab.getNumeroHabitacion(),
                        new ReservaDAO.RangoFechas(hab.getFechaIngreso(), hab.getFechaEgreso())
                );
            }

            // Crear las reservas usando el gestor
            List<ReservaDTO> reservasCreadas = gestorReserva.crearReservasConFechasEspecificas(
                    nombre,
                    apellido,
                    habitacionesConFechas
            );

            // Mostrar confirmación exitosa
            mostrarConfirmacionExitosa(reservasCreadas, nombre, apellido, telefono, existe);

        } catch (Exception e) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "Error al procesar la reserva:\n" + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void crearPersona(String nombre, String apellido, String telefono) {
        try {
            Persona nuevaPersona = Persona.builder()
                    .nombre(nombre)
                    .apellido(apellido)
                    .telefono(telefono.isEmpty() ? null : telefono)
                    .build();

            personaDAO.guardar(nuevaPersona);

            System.out.println("✓ Nueva persona creada: " + nombre + " " + apellido);

        } catch (Exception e) {
            throw new RuntimeException("Error al crear la persona: " + e.getMessage(), e);
        }
    }

    private void mostrarConfirmacionExitosa(List<ReservaDTO> reservasCreadas, String nombre,
                                            String apellido, String telefono, boolean existia) {
        double costoTotal = calcularCostoTotal();
        int totalNoches = calcularTotalNoches();

        String estadoPersona = existia ? "EXISTENTE" : "CREADA";

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("✓ RESERVA CONFIRMADA EXITOSAMENTE\n\n");
        mensaje.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        mensaje.append(String.format("Cliente: %s %s (%s)\n", nombre, apellido, estadoPersona));
        mensaje.append(String.format("Teléfono: %s\n\n",
                telefono.isEmpty() ? "No especificado" : telefono));
        mensaje.append("RESUMEN:\n");
        mensaje.append(String.format("• Reservas creadas: %d\n", reservasCreadas.size()));
        mensaje.append(String.format("• Total noches: %d\n", totalNoches));
        mensaje.append(String.format("• Costo total: $%.2f\n\n", costoTotal));

        // Detalle por habitación
        mensaje.append("DETALLE:\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (HabitacionReservaDTO hab : habitacionesSeleccionadas) {
            mensaje.append(String.format("• Hab %d (%s): %s - %s\n",
                    hab.getNumeroHabitacion(),
                    hab.getTipoHabitacion(),
                    hab.getFechaIngreso().format(formatter),
                    hab.getFechaEgreso().format(formatter)));
        }

        PopUpController.mostrarPopUpConCallback(
                PopUpType.SUCCESS,
                mensaje.toString(),
                confirmado -> {
                    // Limpiar DataTransfer
                    DataTransfer.limpiar();

                    // Mostrar resumen en consola
                    mostrarResumenEnConsola(reservasCreadas, nombre, apellido, telefono, estadoPersona);

                    // Volver al menú
                    HotelPremier.cambiarA("menu");
                }
        );
    }

    private double calcularCostoTotal() {
        return habitacionesSeleccionadas.stream()
                .mapToDouble(HabitacionReservaDTO::getCostoTotal)
                .sum();
    }

    private int calcularTotalNoches() {
        return habitacionesSeleccionadas.stream()
                .mapToInt(HabitacionReservaDTO::getCantidadNoches)
                .sum();
    }

    private void mostrarResumenEnConsola(List<ReservaDTO> reservasCreadas, String nombre,
                                         String apellido, String telefono, String estadoPersona) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("          RESERVA CONFIRMADA - RESUMEN");
        System.out.println("=".repeat(60));
        System.out.printf("Cliente: %s %s (%s)%n", nombre, apellido, estadoPersona);
        System.out.printf("Teléfono: %s%n", telefono.isEmpty() ? "No especificado" : telefono);
        System.out.println("-".repeat(60));
        System.out.printf("Total de reservas creadas: %d%n", reservasCreadas.size());
        System.out.printf("Total de noches: %d%n", calcularTotalNoches());
        System.out.printf("Costo total: $%.2f%n", calcularCostoTotal());
        System.out.println("-".repeat(60));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < habitacionesSeleccionadas.size(); i++) {
            HabitacionReservaDTO hab = habitacionesSeleccionadas.get(i);
            ReservaDTO reserva = reservasCreadas.get(i);

            System.out.printf("\nReserva ID: %d%n", reserva.getId());
            System.out.printf("  Habitación: %d (%s)%n",
                    hab.getNumeroHabitacion(),
                    hab.getTipoHabitacion());
            System.out.printf("  Check-in:  %s%n", hab.getFechaIngreso().format(formatter));
            System.out.printf("  Check-out: %s%n", hab.getFechaEgreso().format(formatter));
            System.out.printf("  Noches:    %d%n", hab.getCantidadNoches());
            System.out.printf("  Costo:     $%.2f%n", hab.getCostoTotal());
        }

        System.out.println("=".repeat(60) + "\n");
    }
}