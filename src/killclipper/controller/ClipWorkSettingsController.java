package killclipper.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import killclipper.model.ClipWorkModel;

public class ClipWorkSettingsController extends PopupViewController implements Initializable {

    @FXML private TextField preceedingSecondsField;
    @FXML private TextField trailingSecondsField;
    @FXML private Button okButton;

    @FXML
    void handleOkAction(ActionEvent event) {
        ClipWorkModel.preceedingSeconds = Integer.parseInt(preceedingSecondsField.getText());
        ClipWorkModel.trailingSeconds = Integer.parseInt(trailingSecondsField.getText());
        close(event);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        assert preceedingSecondsField != null : "fx:id=\"preceedingSecondsField\" was not injected: check your FXML file 'ClipworkSettingsView.fxml'.";
        assert trailingSecondsField != null : "fx:id=\"trailingSecondsField\" was not injected: check your FXML file 'ClipworkSettingsView.fxml'.";
        assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'ClipworkSettingsView.fxml'.";

    }
}