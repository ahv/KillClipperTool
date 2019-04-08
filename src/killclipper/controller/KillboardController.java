package killclipper.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;

public class KillboardController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableColumn<?, ?> timestampColumn;

    @FXML
    private TableColumn<?, ?> continentColumn;

    @FXML
    private TableColumn<?, ?> attackerColumn;

    @FXML
    private TableColumn<?, ?> methodColumn;

    @FXML
    private TableColumn<?, ?> targetColumn;

    @FXML
    void initialize() {
        assert timestampColumn != null : "fx:id=\"timestampColumn\" was not injected: check your FXML file 'KillboardView.fxml'.";
        assert continentColumn != null : "fx:id=\"continentColumn\" was not injected: check your FXML file 'KillboardView.fxml'.";
        assert attackerColumn != null : "fx:id=\"attackerColumn\" was not injected: check your FXML file 'KillboardView.fxml'.";
        assert methodColumn != null : "fx:id=\"methodColumn\" was not injected: check your FXML file 'KillboardView.fxml'.";
        assert targetColumn != null : "fx:id=\"targetColumn\" was not injected: check your FXML file 'KillboardView.fxml'.";

    }
}
