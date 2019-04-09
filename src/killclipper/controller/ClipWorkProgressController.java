package killclipper.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import killclipper.ClipWork;
import killclipper.ClipWork.ClipJob;
import killclipper.Clipper;
import killclipper.Main;
import killclipper.model.ClipWorkModel;
import killclipper.model.SettingsModel;

public class ClipWorkProgressController extends PopupViewController {

    @FXML
    private VBox elementBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button outputFolderButton;

    @FXML
    private Label outputFolderPathLabel;

    @FXML
    private Button startButton;
    private ClipWork clipWork;

    @FXML
    void handleCancelAction(ActionEvent event) {
        close(event);
        Clipper.abortWork();
    }

    // TODO: Path is already written into the ClipJobs at ClipWork object creation (which happens in the SyncView)
    // so this one does nothing!
    @FXML
    void handleOutputFolderAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        // TODO: Doesn't actually belong to main stage! Add some stage getter to PopupViewController
        File dir = directoryChooser.showDialog(Main.mainStage);
        if (dir == null) {
            return;
        }
        SettingsModel.getSettings().setVideoOutputRootPath(dir.getAbsolutePath());
        outputFolderPathLabel.setText(dir.getAbsolutePath());
    }

    @FXML
    void handleStartAction(ActionEvent event) throws IOException {
        startButton.setDisable(true);
        Clipper.startWork(clipWork, () -> {
            Platform.runLater(() -> {
                close(event);
                try {
                    Main.popupView("ClipWorkDoneDialogView");
                } catch (IOException ex) {
                    Logger.getLogger(ClipWorkProgressController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        });
    }

    @FXML
    void initialize() {
        clipWork = ClipWorkModel.clipWork;
        initializeClipElements();
        outputFolderPathLabel.setText(SettingsModel.getSettings().getVideoOutputRootPath());
    }

    void initializeClipElements() {
        List<Node> elements = new ArrayList<>();
        for (ClipWork.ClipJob cj : clipWork.getClipJobs()) {
            elements.add(createClipJobElement(cj));
        }
        elementBox.getChildren().addAll(elements);

        // Hax to get the scroll pane to show the last element
        Region r = new Region();
        r.setMinHeight(20);
        elementBox.getChildren().add(r);
    }

    private Pane createClipJobElement(ClipJob clipJob) {
        // TODO: Set element widths based on parent
        double parentWidth = elementBox.getWidth();
        Label clipNameLabel = new Label(clipJob.getClipName());
        clipNameLabel.prefWidth(90);
        clipNameLabel.prefHeight(20);
        Label durationLabel = new Label(clipJob.getClipDurationSeconds() + " seconds");
        durationLabel.prefWidth(90);
        durationLabel.prefHeight(20);
        durationLabel.setLayoutX(95);
        ProgressBar jobProgressBar = new ProgressBar(0);
        jobProgressBar.progressProperty().unbind();
        clipJob.getProgressProperty().addListener((observable) -> {
            // Platform.runLater(() -> ... ); this?
            SimpleDoubleProperty v = (SimpleDoubleProperty) observable;
            jobProgressBar.setProgress(v.get());
        });
        jobProgressBar.prefWidth(300);
        jobProgressBar.prefHeight(20);
        jobProgressBar.setLayoutX(220);
        jobProgressBar.setStyle("-fx-accent: green;");
        Pane elementPane = new Pane(clipNameLabel, durationLabel, jobProgressBar);
        elementPane.prefWidth(parentWidth);
        elementPane.prefHeight(25);
        elementPane.minHeight(25);

        return elementPane;
    }
}
