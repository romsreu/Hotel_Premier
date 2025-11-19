module ar.utn.hotel.hotel_premier {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.net.http;
    requires java.desktop;

    opens ar.utn.hotel to javafx.fxml;
    opens controllers to javafx.fxml;
    opens controllers.BuscarHuesped to javafx.fxml;

    exports ar.utn.hotel;
    exports controllers;
    exports controllers.EstadoHabitaciones;
    opens controllers.EstadoHabitaciones to javafx.fxml;
}
