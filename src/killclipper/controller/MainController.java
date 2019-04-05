package killclipper.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import killclipper.ApiCaller;
import killclipper.Main;
import killclipper.Settings;
import killclipper.Video;
import killclipper.model.MediaModel;
import killclipper.model.SettingsModel;

public class MainController implements Initializable {

    @FXML private TableView characterTable;
    // TODO: characterList could go to SettingsModel to clean this class up
    private ObservableList<Settings.PlayerCharacter> characterList;
    @FXML private TableColumn<Settings.PlayerCharacter, String> characterTableNameColumn;
    @FXML private TableColumn<Settings.PlayerCharacter, String> characterTableIdColumn;
    @FXML private TableColumn<Settings.PlayerCharacter, Boolean> characterTableEnabledColumn;
    @FXML private TextField addCharacterInputField;
    @FXML private Button addCharacterButton;
    @FXML private Button removeCharacterButton;
    @FXML private Button enableCharacterButton;
    
    @FXML private Button openFileButton;
    @FXML private MediaView videoFilePreview;
    @FXML private Label videoInfoLabel;
    
    @FXML private Button syncViewButton;

    @FXML
    private void handleAddCharacterAction(ActionEvent event) {
        // TODO: Validation
        String nameInput = addCharacterInputField.getText();
        // TODO: Make Async
        String id = ApiCaller.getCharacterIdForName(nameInput);
        Settings.PlayerCharacter newCharacter = new Settings.PlayerCharacter(nameInput, id);
        characterList.add(newCharacter);
        SettingsModel.getSettings().save();
        addCharacterInputField.clear();
    }

    @FXML
    private void handleRemoveCharacterAction(ActionEvent event) {
        // TODO: Validation
        int index = characterTable.getSelectionModel().getSelectedIndex();
        if (index < 0) return;
        characterList.remove(index);
        SettingsModel.getSettings().save();
    }
    
    @FXML
    private void handleEnableCharacterAction(ActionEvent event) {
        int index = characterTable.getSelectionModel().getSelectedIndex();
        if (index < 0) return;
        characterList.get(index).setEnabled(!characterList.get(index).isEnabled());
        SettingsModel.getSettings().save();
        characterTable.refresh();
    }
    
    @FXML
    private void handleOpenFileAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open video");
        fileChooser.setInitialDirectory(new File(SettingsModel.getSettings().getVideoSourceRootPath()));
        File f = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());
        if (f == null) return;
        SettingsModel.getSettings().setVideoSourceRootPath(f.getParent());
        SettingsModel.getSettings().save();
        Video video = new Video(f);
        MediaModel.setVideo(video);
        videoInfoLabel.setText(video.toString());
        Media media = new Media(f.toURI().toString());
        MediaModel.setMedia(media);
        MediaPlayer mp = new MediaPlayer(media);
        videoFilePreview.setMediaPlayer(mp);
        mp.seek(Duration.seconds(30));
        mp.volumeProperty().set(0);
        mp.play();
        syncViewButton.setDisable(false);
    }
    
    @FXML
    private void handleSyncViewAction(ActionEvent event) throws IOException {
        Main.changeView("SyncView");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Initializing MainViewController.");
        Main.mainStage.setResizable(false);
        initializeCharactersTable();
    }

    private void initializeCharactersTable() {
        characterList = FXCollections.observableList(SettingsModel.getSettings().getCharacters());
        characterTableNameColumn.setCellValueFactory(new PropertyValueFactory("name"));
        characterTableIdColumn.setCellValueFactory(new PropertyValueFactory("id"));
        characterTableEnabledColumn.setCellValueFactory(new PropertyValueFactory("enabled"));
        characterTable.getColumns().setAll(characterTableNameColumn, characterTableIdColumn, characterTableEnabledColumn);
        characterTable.setItems(characterList);
    }
}
