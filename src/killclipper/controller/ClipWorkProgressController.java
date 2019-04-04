package killclipper.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import killclipper.ClipWork;
import killclipper.ClipWork.Span;
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
    
    private Pane createElement(String label) {
        Label elementLabel = new Label(label);
        elementLabel.prefWidth(81);
        elementLabel.prefHeight(20);
        elementLabel.minHeight(20);
        ProgressBar elementProgress = new ProgressBar();
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
        elementBox.setSpacing(20);
        clipWork = ClipWorkModel.clipWork;
        int i = 0;
        List<Node> elements = new ArrayList<>();
        for (Span s : clipWork.getSpans()) {
            elements.add(createElement("clip_" + i));
            i++;
        }
        elementBox.getChildren().addAll(elements);
    }
}