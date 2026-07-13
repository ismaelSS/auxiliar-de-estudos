module org.IsmaelSS.flashCardJava {
    requires javafx.controls;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires java.logging;
    requires java.desktop;

    opens org.IsmaelSS.model to com.fasterxml.jackson.databind;
    exports org.IsmaelSS;
}
