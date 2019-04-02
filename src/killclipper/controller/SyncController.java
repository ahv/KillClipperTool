package killclipper.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import killclipper.ApiCaller;
import killclipper.Clipper;
import killclipper.Killboard;
import killclipper.Killboard.Entry;
import killclipper.Main;
import killclipper.Settings;
import killclipper.Video;
import killclipper.model.ClipWorkModel;
import killclipper.model.MediaModel;
import killclipper.model.SettingsModel;

public class SyncController implements Initializable  {

    @FXML private StackPane mediaPane;
    @FXML private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    
    
    @FXML private Pane killTimelinePane;
    // TODO: refactor this shit outta heeeere
    private Killboard killboard;
    
    @FXML private Slider scrubber;
    @FXML private Button playPauseButton;
    @FXML private Button previousKillButton;
    @FXML private Button nextKillButton;
    @FXML private Button seekBackButton;
    @FXML private Button seekForwardButton;
    @FXML private Button stepBackButton;
    @FXML private Button stepForwardButton;
    
    @FXML private Button previousSyncTargetButton;
    @FXML private Button nextSyncTargetButton;
    @FXML private Button syncButton;
    @FXML private Label syncTargetLabel;
    private int syncTargetIndex;
    @FXML private Label syncTimeLabel;
    
    @FXML private Button settingsButton;
    @FXML private Button generateButton;
    
    @FXML private Button fullscreenButton;
    
    @FXML
    void handlePlayPauseAction(ActionEvent event) {
        if (mediaPlayer.getStatus().equals(Status.PAUSED) || mediaPlayer.getStatus().equals(Status.READY)) {
            mediaPlayer.play();
            playPauseButton.setText("||");
        } else {
            mediaPlayer.pause();
            playPauseButton.setText(">");
        }
        
    }
    
    @FXML
    void handleScrubberAction(ActionEvent event) {
        //mediaPlayer.seek(Duration.seconds(scrubber.getValue()));
    }
    
    @FXML
    void handleStepBackAction(ActionEvent event) {
        mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() - 0.25));
    }

    @FXML
    void handleStepForwardAction(ActionEvent event) {
        mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() + 0.25));
    }
    
    @FXML
    void handleSeekBackAction(ActionEvent event) {
        mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() - 2));
    }

    @FXML
    void handleSeekForwardAction(ActionEvent event) {
        mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() + 2));
    }

    @FXML
    void handlePreviousKillAction(ActionEvent event) {
        long videoStartTimestamp = MediaModel.getVideo().getStartTimestamp();
        long playerCurrentTime = (long) mediaPlayer.getCurrentTime().toSeconds();
        long previousKillTimestamp = killboard.lastTimestampBefore(videoStartTimestamp + playerCurrentTime);
        long seekTo = previousKillTimestamp - videoStartTimestamp;
        mediaPlayer.seek(Duration.seconds(seekTo));
    }

    @FXML
    void handleNextKillAction(ActionEvent event) {
        long videoStartTimestamp = MediaModel.getVideo().getStartTimestamp();
        long playerCurrentTime = (long) mediaPlayer.getCurrentTime().toSeconds();
        long nextKillTimestamp = killboard.nextTimestampAfter(videoStartTimestamp + playerCurrentTime);
        long seekTo = nextKillTimestamp - videoStartTimestamp;
        mediaPlayer.seek(Duration.seconds(seekTo));
    }
    
    @FXML
    void handlePreviousSyncTargetAction(ActionEvent event) {
        syncTargetIndex = (syncTargetIndex - 1) <= 0 ? 0 : syncTargetIndex -1;
        String syncTargetName = killboard.entries.get(syncTargetIndex).character.name.first;
        syncTargetLabel.setText("Target: " + syncTargetName);
    }

    @FXML
    void handleNextSyncTargetAction(ActionEvent event) {
        syncTargetIndex = (syncTargetIndex + 1) > killboard.entries.size() ? killboard.entries.size() - 1 : syncTargetIndex + 1;
        String syncTargetName = killboard.entries.get(syncTargetIndex).character.name.first;
        syncTargetLabel.setText("Target: " + syncTargetName);
    }

    @FXML
    void handleSyncAction(ActionEvent event) {
        int videoTime = (int) mediaPlayer.getCurrentTime().toSeconds();
        long killOffset = killboard.entries.get(syncTargetIndex).timestamp - MediaModel.getVideo().getStartTimestamp();
        long correction = killOffset - videoTime;
        syncTimeLabel.setText("Sync: " + correction + " seconds");
        MediaModel.getVideo().correctTimeBy(correction);
    }
    
    @FXML
    void handleShowSettingsAction(ActionEvent event) throws IOException {
        Main.popupView("ClipworkSettingsView");
    }
    
    @FXML
    void handleGenerateClipworkAction(ActionEvent event) throws IOException {
        mediaPlayer.pause();
        ClipWorkModel.generate(killboard);
        Main.popupView("ClipWorkProgressView");
    }
    
    /*
    @FXML
    void handleStartClipWorkAction(ActionEvent event) {
        Clipper.instance.queueClibJobs(MediaModel.getVideo(), ClipWorkModel.clipWork);
        Clipper.instance.startClipWork();
        Main.mainStage.close();
    }
    */
    
    @FXML
    void handleFullscreenAction (ActionEvent event) {
        Main.mainStage.setFullScreen(!Main.mainStage.isFullScreen());
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing SyncViewController.");
        Main.mainStage.setResizable(true);
        
        fetchKillboard();
        initializeMediaPlayer();
        
        // TODO: Refactor setting sync target
        syncTargetIndex = 0;
        String syncTargetName = killboard.entries.get(syncTargetIndex).character.name.first;
        syncTargetLabel.setText("Target: " + syncTargetName);
        //
        
        initializeTimeline();
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer(MediaModel.getMedia());
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.fitWidthProperty().bind(mediaPane.widthProperty());
        mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
        
        scrubber.setMax(MediaModel.getVideo().getDuration()); 
    }

    private void fetchKillboard() {
        Video video = MediaModel.getVideo();
        long startTimestamp = video.getStartTimestamp();
        long endTimestamp = video.getEndTimeStamp();
        killboard = new Killboard();
        for (Settings.PlayerCharacter pc : SettingsModel.getSettings().getCharacters()) {
            killboard.append(ApiCaller.getKillboard(pc.getId(), startTimestamp, endTimestamp));
        }
    }

    private void initializeTimeline() {
        // TODO: getPaneWidth will be 0 here since the pane hasn't been laid out yet, figure out workaround
        //long paneWidth = (long) killTimelinePane.getWidth();
        float paneWidth = 950;
        System.out.println("Pane Width: " + paneWidth);
        float span = MediaModel.getVideo().getDuration();
        System.out.println("Span: " + span);
        for (Entry e : killboard.entries) {
            float pos = e.timestamp - MediaModel.getVideo().getStartTimestamp();
            float spot = (long) ((pos/span) * paneWidth);
            Line line = new Line(spot, 7, spot, 23);
            line.setStyle(e.table_type.equals("kills") ? "-fx-stroke: green" : "-fx-stroke: red");
            killTimelinePane.getChildren().add(line);
        }
    }
}
