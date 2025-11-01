module ar.utn.hotel.hotel_premier {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires static lombok;

    opens ar.utn.hotel.hotel_premier to javafx.fxml;
    exports ar.utn.hotel.hotel_premier;
}