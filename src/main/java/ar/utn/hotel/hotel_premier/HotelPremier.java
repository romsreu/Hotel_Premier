package ar.utn.hotel.hotel_premier;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.SceneManager;
import javafx.scene.Scene;

public class HotelPremier extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.precargarEscena("menu", "/views/interfaces/menu-principal.fxml");
        SceneManager.precargarEscena("alta_huesped", "/views/interfaces/dar-alta-huesped.fxml");
        Scene escenaInicial = SceneManager.getEscena("menu");
        stage.setTitle("Hotel Premier");
        stage.setScene(escenaInicial);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
