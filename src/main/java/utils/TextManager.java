package utils;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import java.util.Locale;

/**
 * Utilidades para manipular texto en campos JavaFX.
 * Incluye conversión automática a mayúsculas.
 */
public class TextManager {

    private static final Locale LOCALE_ES_AR = new Locale("es", "AR");

    /** Aplica conversión automática a mayúsculas en todos los TextField indicados. */
    public static void aplicarMayusculas(TextField... campos) {
        for (TextField campo : campos) {
            if (campo == null) continue;
            campo.setTextFormatter(new TextFormatter<>(cambio -> {
                String texto = cambio.getText();
                if (texto != null) {
                    cambio.setText(texto.toUpperCase(LOCALE_ES_AR));
                }
                return cambio;
            }));
        }
    }
    public static void aplicarMascaraDNI(TextField tf) {
        final Pattern maxDigits = Pattern.compile("\\d{0,8}");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            // Propuesta tras el cambio (incluye puntos actuales, etc.)
            String proposedFormatted = change.getControlNewText();
            // Solo dígitos de la propuesta
            String newDigits = proposedFormatted.replaceAll("\\D", "");

            if (!maxDigits.matcher(newDigits).matches()) return null;

            // Formatear a DNI
            String formatted = formatDNI(newDigits);

            // ----- Cálculo de caret -----
            // 1) ¿cuántos dígitos hay antes del caret propuesto?
            int digitIndex = countDigitsUpTo(proposedFormatted, change.getCaretPosition());
            // 2) En el texto ya formateado, ¿en qué índice cae ese dígito?
            int newCaret = caretPosForDigitIndex(formatted, digitIndex);

            // Reemplazar todo el contenido por el formateado
            change.setText(formatted);
            change.setRange(0, change.getControlText().length());

            // Fijar caret y anchor
            change.setCaretPosition(newCaret);
            change.setAnchor(newCaret);

            return change;
        };

        tf.setTextFormatter(new TextFormatter<>(filter));
    }

    private static String formatDNI(String digits) {
        if (digits.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(digits);
        if (digits.length() > 2) sb.insert(2, ".");
        if (digits.length() > 5) sb.insert(6, ".");
        return sb.toString();
    }

    /* ===================== CUIT ===================== */

    /** Aplica máscara CUIT: ##-########-# (máx 11 dígitos) con caret correcto. */
    public static void aplicarMascaraCUIT(TextField tf) {
        final Pattern maxDigits = Pattern.compile("\\d{0,11}");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String proposedFormatted = change.getControlNewText();
            String newDigits = proposedFormatted.replaceAll("\\D", "");

            if (!maxDigits.matcher(newDigits).matches()) return null;

            String formatted = formatCUIT(newDigits);

            int digitIndex = countDigitsUpTo(proposedFormatted, change.getCaretPosition());
            int newCaret = caretPosForDigitIndex(formatted, digitIndex);

            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(newCaret);
            change.setAnchor(newCaret);

            return change;
        };

        tf.setTextFormatter(new TextFormatter<>(filter));
    }

    private static String formatCUIT(String digits) {
        if (digits.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(digits);
        if (digits.length() > 2) sb.insert(2, "-");
        if (digits.length() > 10) sb.insert(11, "-");
        return sb.toString();
    }

    /* ===================== Helpers comunes ===================== */

    /** Cuenta cuántos dígitos hay en s en el rango [0, upTo). */
    private static int countDigitsUpTo(String s, int upTo) {
        int count = 0;
        int limit = Math.min(upTo, s.length());
        for (int i = 0; i < limit; i++) {
            if (Character.isDigit(s.charAt(i))) count++;
        }
        return count;
    }

    /**
     * Dado un texto formateado y un índice de dígito (0..N),
     * devuelve el índice de caret justo después de ese dígito en el string formateado.
     * Si digitIndex es 0, devuelve el primer índice válido (antes de todo).
     */
    private static int caretPosForDigitIndex(String formatted, int digitIndex) {
        if (digitIndex <= 0) return 0;
        int count = 0;
        for (int i = 0; i < formatted.length(); i++) {
            if (Character.isDigit(formatted.charAt(i))) {
                count++;
                if (count == digitIndex) {
                    // caret va después de este dígito
                    return i + 1;
                }
            }
        }
        // Si piden más que la cantidad de dígitos, ir al final
        return formatted.length();
    }

    public static void limitarCaracteres(TextField tf, int maxLength) {
        tf.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > maxLength) {
                tf.setText(newText.substring(0, maxLength));
            }
        });
    }
}


