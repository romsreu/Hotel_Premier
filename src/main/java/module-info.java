module ar.utn.hotel.hotel_premier {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires static lombok;
    requires jakarta.persistence;
    requires jakarta.transaction;
    requires ar.utn.hotel.hotel_premier;

    opens ar.utn.hotel.hotel_premier to javafx.fxml;
    exports ar.utn.hotel.hotel_premier;
    exports ar.utn.hotel;
    opens ar.utn.hotel to javafx.fxml;
}