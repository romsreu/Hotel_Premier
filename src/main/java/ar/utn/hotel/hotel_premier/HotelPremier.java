package ar.utn.hotel.hotel_premier;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class HotelPremier extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlUrl = getClass().getResource("/views/interfaces/dar-alta-huesped.fxml");
        System.out.println("FXML URL: " + fxmlUrl);

        if (fxmlUrl == null) {
            throw new RuntimeException("No se encontró el archivo FXML");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load());

        stage.setTitle("Hotel Premier - Alta de Huésped");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
