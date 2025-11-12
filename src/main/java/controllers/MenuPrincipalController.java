package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utils.SceneManager; // usa el gestor de escenas

public class MenuPrincipalController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private void on_alta_huesped_pressed(ActionEvent event) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setScene(SceneManager.getEscena("alta_huesped"));
        stage.show();
    }
}
