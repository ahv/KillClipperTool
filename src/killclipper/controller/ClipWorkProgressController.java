package killclipper.controller;

import java.util.ArrayList;
import java.util.List;
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
import killclipper.ClipWork;
import killclipper.ClipWork.ClipJob;
import killclipper.model.ClipWorkModel;

public class ClipWorkProgressController {


    @FXML
    private VBox elementBox;
    
    @FXML
    private Button cancelButton;

    @FXML
    private Button startButton;
    private ClipWork clipWork;

    @FXML
    void handleCancelAction(ActionEvent event) {

    }

    @FXML
    void handleStartAction(ActionEvent event) {
        startButton.setDisable(true);
        clipWork.start();
        
    }
    
    private Pane createClipJobElement(ClipJob clipJob) {
        Label elementLabel = new Label(clipJob.getClipName());
        elementLabel.prefWidth(81);
        elementLabel.prefHeight(20);
        elementLabel.minHeight(20);
        ProgressBar elementProgress = new ProgressBar();
        elementProgress.progressProperty().unbind();
        clipJob.getProgressProperty().addListener((observable) -> {
            SimpleDoubleProperty v = (SimpleDoubleProperty) observable;
            elementProgress.setProgress(v.get());
        });
        //elementProgress.progressProperty().bind(clipJob.progress);
        elementProgress.prefWidth(190);
        elementProgress.minWidth(190);
        elementProgress.prefHeight(20);
        elementProgress.minHeight(20);
        elementProgress.setLayoutX(85);
        elementProgress.setStyle("-fx-accent: green;");
        elementProgress.setProgress(0);
        Pane elementPane = new Pane(elementLabel, elementProgress);
        elementPane.prefWidth(282);
        elementPane.prefHeight(25);
        elementPane.minHeight(25);
        
        return elementPane;
    }

    @FXML
    void initialize() {    
        clipWork = ClipWorkModel.clipWork;
        List<Node> elements = new ArrayList<>();
        for (ClipJob cj : clipWork.getClipJobs()) {
            elements.add(createClipJobElement(cj));
        }
        
        elementBox.getChildren().addAll(elements);
        // Hax to get the scroll pane to show the last element
        Region r = new Region();
        r.setMinHeight(20);
        elementBox.getChildren().add(r);
    }
}