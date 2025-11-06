module simulator.cws {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens simulator.cws to javafx.fxml;
    exports simulator.cws;
    exports simulator.cws.ui;
    opens simulator.cws.ui to javafx.fxml;
    exports simulator.cws.models;
    opens simulator.cws.models to javafx.fxml;
    exports simulator.cws.utlils;
    opens simulator.cws.utlils to javafx.fxml;
}