package killclipper.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ClipWorkDoneDialogController extends PopupViewController {

    @FXML
    private Button okButton;

    @FXML
    void okAction(ActionEvent event) {
        close(event);
    }

    @FXML
    void initialize() {
    }
}