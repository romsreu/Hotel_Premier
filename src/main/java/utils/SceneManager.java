package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    private static final Map<String, Scene> escenas = new HashMap<>();

    public static void precargarEscena(String nombre, String rutaFXML) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(rutaFXML));
        Parent root = loader.load();
        escenas.put(nombre, new Scene(root));
    }

    public static Scene getEscena(String nombre) {
        return escenas.get(nombre);
    }
}
