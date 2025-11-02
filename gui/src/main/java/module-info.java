module org.example.gui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.gui to javafx.fxml;
    exports org.example.gui;
}