module com.dsoftn.journal2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires com.google.gson;
    requires okhttp3;
    requires org.json;

    opens com.dsoftn.controllers to javafx.fxml;
    opens com.dsoftn to javafx.fxml;
    exports com.dsoftn;
}


