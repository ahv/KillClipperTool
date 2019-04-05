package killclipper.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import killclipper.model.SettingsModel;

public class ClipWorkSettingsController extends PopupViewController implements Initializable {

    @FXML
    private TextField preceedingSecondsField;
    @FXML
    private TextField trailingSecondsField;
    @FXML
    private Button okButton;
    @FXML
    private CheckBox combinedVideoCheck;

    @FXML
    void handleOkAction(ActionEvent event) {
        // TODO: Validate
        String ps = preceedingSecondsField.getText();
        String ts = trailingSecondsField.getText();
        SettingsModel.getSettings().setPreceedingSeconds(Integer.parseInt(ps));
        SettingsModel.getSettings().setTrailingSeconds(Integer.parseInt(ts));
        SettingsModel.getSettings().save();
        close(event);
    }

    @FXML
    void handleToggleCombinedVideoAction(ActionEvent event) {
        SettingsModel.getSettings().setCreateCombinedVideo(combinedVideoCheck.isSelected());
        SettingsModel.getSettings().save();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        preceedingSecondsField.setText("" + SettingsModel.getSettings().getPreceedingSeconds());
        trailingSecondsField.setText("" + SettingsModel.getSettings().getTrailingSeconds());
        combinedVideoCheck.setSelected(SettingsModel.getSettings().isCreateCombinedVideo());
    }
}
