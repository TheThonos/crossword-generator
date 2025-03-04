module dev.cqb13.crosswordgenerator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens dev.cqb13.crosswordgenerator to javafx.fxml;
    exports dev.cqb13.crosswordgenerator;
}